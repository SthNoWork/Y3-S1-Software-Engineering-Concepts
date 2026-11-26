package edu.itc.salesreport;

import edu.itc.salesreport.engine.SequentialReportEngine;
import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Prints the monthly report of the sequential engine: the baseline numbers. */
public final class ReportMain {
    public static void main(String[] args) {
        Path data = Path.of(args.length > 0 ? args[0] : "data");
        YearMonth month = YearMonth.parse(args.length > 1 ? args[1] : "2026-09");
        long start = System.currentTimeMillis();
        MonthlyReport r = new SequentialReportEngine().generate(month, data);
        long ms = System.currentTimeMillis() - start;
        List<BranchSummary> all = new ArrayList<>(r.branches());
        all.add(r.chain());
        for (BranchSummary b : all) {
            System.out.printf("%-5s revenue %11s  discounts %9s  receipts %,7d  basket %5s%n",
                    b.branch(), b.revenue(), b.discounts(), b.receipts(), b.averageBasket());
        }
        System.out.println("anomalies: " + r.anomalies().size() + ", time: " + ms + " ms");
    }
}
