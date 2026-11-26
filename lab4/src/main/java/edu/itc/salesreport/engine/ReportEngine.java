package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.MonthlyReport;

import java.nio.file.Path;
import java.time.YearMonth;

/** Builds the monthly report from data/yyyy-MM/*.csv. */
public interface ReportEngine {
    MonthlyReport generate(YearMonth month, Path dataDir);
}
