package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.MonthlyReport;
import edu.itc.salesreport.model.SaleTransaction;

import java.time.YearMonth;

/** Collects totals: add rows, then build the report. NOT thread-safe. */
public interface SalesAggregator {
    void add(SaleTransaction tx);

    void addInvalid(String anomaly);

    MonthlyReport toReport(YearMonth month);
}
