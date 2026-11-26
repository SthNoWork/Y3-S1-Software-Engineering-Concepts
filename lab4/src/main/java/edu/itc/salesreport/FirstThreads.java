package edu.itc.salesreport;

import edu.itc.salesreport.engine.DataFiles;
import edu.itc.salesreport.engine.MonthAggregator;
import edu.itc.salesreport.engine.SequentialReportEngine;
import edu.itc.salesreport.model.MonthlyReport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Lab-04 Task 1: one thread per branch, start() and join(). */
public final class FirstThreads {
    static final YearMonth MONTH = YearMonth.of(2026, 9);
    static final Path DATA = Path.of("data");
    static final List<String> BRANCHES = List.of("BTB", "PNH", "REP");

    /** The job of one thread: every file of one branch, into one aggregator. */
    static MonthAggregator branchWork(String branch) throws IOException {
        MonthAggregator agg = new MonthAggregator();
        List<Path> files = DataFiles.list(MONTH, DATA, branch);
        for (int i = 0; i < files.size(); i++) {
            agg.merge(DataFiles.aggregateFile(files.get(i)));
            if ((i + 1) % 10 == 0) {
                String me = Thread.currentThread().getName();
                System.out.println(me + " finished file " + (i + 1) + " of " + files.size());
            }
        }
        return agg;
    }

    public static void main(String[] args) throws InterruptedException {
        MonthAggregator[] results = new MonthAggregator[BRANCHES.size()];
        List<Thread> threads = new ArrayList<>();
        long start = System.currentTimeMillis();

        for (int i = 0; i < BRANCHES.size(); i++) {
            int slot = i;
            String branch = BRANCHES.get(i);
            Thread worker = new Thread(() -> {
                try {
                    results[slot] = branchWork(branch);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, "worker-" + branch);
            threads.add(worker);
            worker.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        MonthAggregator total = new MonthAggregator();
        for (MonthAggregator part : results) {
            total.merge(part);
        }
        MonthlyReport report = total.toReport(MONTH);
        long ms = System.currentTimeMillis() - start;

        MonthlyReport expected = new SequentialReportEngine().generate(MONTH, DATA);
        System.out.println("chain revenue " + report.chain().revenue() + " in " + ms + " ms");
        System.out.println("same report as the sequential engine: " + report.equals(expected));
    }
}
