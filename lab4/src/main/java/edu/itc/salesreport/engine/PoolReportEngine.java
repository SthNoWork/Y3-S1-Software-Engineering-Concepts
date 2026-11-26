package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.MonthlyReport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Lab-04 Task 2: split (one task per file), then combine. Used again in Labs 05 to 08. */
public final class PoolReportEngine implements ReportEngine {
    private final int threads;

    public PoolReportEngine(int threads) {
        this.threads = threads;
    }

    @Override
    public MonthlyReport generate(YearMonth month, Path dataDir) {
        try (ExecutorService pool = Executors.newFixedThreadPool(threads)) {
            // 1. split: one task per file; each task returns its OWN result
            List<Future<MonthAggregator>> futures = new ArrayList<>();
            for (Path file : DataFiles.list(month, dataDir)) {
                futures.add(pool.submit(() -> DataFiles.aggregateFile(file)));
            }
            // 2. combine: wait for each result and merge it into the total
            MonthAggregator total = new MonthAggregator();
            for (Future<MonthAggregator> future : futures) {
                MonthAggregator part = future.get();   // waits for that task
                total.merge(part);
            }
            return total.toReport(month);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ExecutionException e) {          // a task threw an exception
            throw new IllegalStateException("a file task failed", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // keep the "interrupted" flag
            throw new IllegalStateException("interrupted", e);
        }
    }
}