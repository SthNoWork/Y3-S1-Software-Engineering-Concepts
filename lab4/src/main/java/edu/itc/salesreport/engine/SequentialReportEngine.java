package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.MonthlyReport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.YearMonth;

/** The baseline: one thread, one file after the other. Every other engine must equal it. */
public final class SequentialReportEngine implements ReportEngine {

    @Override
    public MonthlyReport generate(YearMonth month, Path dataDir) {
        MonthAggregator total = new MonthAggregator();
        try {
            for (Path file : DataFiles.list(month, dataDir)) {
                FileContent content = DataFiles.read(file);
                DataFiles.parse(content, total);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return total.toReport(month);
    }
}
