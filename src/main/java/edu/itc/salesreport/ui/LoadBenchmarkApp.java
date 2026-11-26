package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ingest.MonthData;
import java.nio.file.Path;
import java.time.Clock;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Measurement harness comparing CallerThreadJobs vs FxBackgroundJobs. */
public class LoadBenchmarkApp extends Application {
    private static final int WARMUP_RUNS = 2;
    private static final int MEASURED_RUNS = 5;

    private ExecutorService workers;

    @Override
    public void start(Stage stage) {
        Path dataDir = Path.of(getParameters().getNamed().getOrDefault("data", "data"));
        YearMonth targetMonth = YearMonth.parse(getParameters().getNamed().getOrDefault("month", "2026-08"));

        var reader = new CsvMonthReader(new CsvTransactionParser());
        var aggregator = new DefaultSalesAggregator();
        SummarySource source = (month, branch) -> {
            MonthData data = reader.read(dataDir, month);
            return aggregator.aggregate(branch, data.rows(), data.rejected().size());
        };

        workers = Executors.newVirtualThreadPerTaskExecutor();
        FrameGapMonitor monitor = new FrameGapMonitor();
        monitor.start();

        System.out.println("mode\trun\tload ms\tmax frame gap ms");
        runSequence("caller-thread", source, new CallerThreadJobs(), targetMonth, monitor, stage, () ->
                runSequence("fx-task", source, new FxBackgroundJobs(workers), targetMonth, monitor, stage, () -> {
                    monitor.stop();
                    Platform.exit();
                }));
    }

    private void runSequence(
            String mode,
            SummarySource source,
            BackgroundJobs jobs,
            YearMonth targetMonth,
            FrameGapMonitor monitor,
            Stage stage,
            Runnable onComplete
    ) {
        ReportViewModel vm = new ReportViewModel(source, jobs, Clock.systemDefaultZone());
        stage.setScene(new Scene(new ReportView(vm).root(), 960, 600));
        stage.show();

        List<Long> loadTimes = new ArrayList<>();
        List<Double> maxGaps = new ArrayList<>();

        executeRun(mode, vm, targetMonth, monitor, 1, WARMUP_RUNS + MEASURED_RUNS, loadTimes, maxGaps, () -> {
            double meanLoad = loadTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            double worstGap = maxGaps.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            System.out.printf(Locale.US, "%s mean load %.0f ms, worst frame gap %.0f ms%n",
                    mode, meanLoad, worstGap);
            onComplete.run();
        });
    }

    private void executeRun(
            String mode,
            ReportViewModel vm,
            YearMonth targetMonth,
            FrameGapMonitor monitor,
            int iteration,
            int totalIterations,
            List<Long> loadTimes,
            List<Double> maxGaps,
            Runnable onDone
    ) {
        if (iteration > totalIterations) {
            onDone.run();
            return;
        }
        monitor.reset();
        long start = System.nanoTime();
        vm.monthProperty().set(targetMonth);

        vm.loadingProperty().addListener((obs, wasLoading, isLoading) -> {
            if (wasLoading && !isLoading) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                double gapMs = monitor.maxGapMillis();
                if (iteration > WARMUP_RUNS) {
                    int measuredIdx = iteration - WARMUP_RUNS;
                    loadTimes.add(elapsedMs);
                    maxGaps.add(gapMs);
                    System.out.printf(Locale.US, "%s\t%d\t%d\t%.0f%n", mode, measuredIdx, elapsedMs, gapMs);
                }
                Platform.runLater(() -> executeRun(
                        mode, vm, targetMonth, monitor, iteration + 1, totalIterations, loadTimes, maxGaps, onDone));
            }
        });
        vm.generateCommand().execute();
    }

    @Override
    public void stop() {
        if (workers != null) {
            workers.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
