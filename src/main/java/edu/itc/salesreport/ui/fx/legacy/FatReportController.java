package edu.itc.salesreport.ui.fx.legacy;

import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportCommand;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesModel;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Legacy FatReportController refactored to delegate parsing, aggregation,
 * and command orchestration to ReportController and SalesModel.
 */
public class FatReportController implements ErrorDisplay {
    private final ReportController delegate;
    private final SalesModel model;
    private String lastError = "";

    public FatReportController(SalesModel model, Path dataDir) {
        this.model = model;
        this.delegate = new ReportController(model, dataDir, Clock.systemDefaultZone(), this);
    }

    public void handleMonthChange(String monthInput) {
        delegate.monthEntered(monthInput);
    }

    public void handleBranchChange(String branchInput) {
        delegate.branchChosen(branchInput);
    }

    public void reload() {
        if (model.currentMonth() != null) {
            delegate.execute(new ReportCommand.LoadMonth(model.currentMonth()));
        }
    }

    @Override
    public void showError(String message) {
        this.lastError = message;
    }

    @Override
    public void clearError() {
        this.lastError = "";
    }

    public String getLastError() {
        return lastError;
    }

    public SalesModel getModel() {
        return model;
    }
}

