package edu.itc.salesreport.ui;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Clock;

public class ReportController {
    private final SalesModel model;
    private final Path dataDir;
    private final Clock clock;
    private final ErrorDisplay errors;

    public ReportController(SalesModel model, Path dataDir, Clock clock, ErrorDisplay errors) {
        this.model = model;
        this.dataDir = dataDir;
        this.clock = clock;
        this.errors = errors;
    }

    public void monthEntered(String text) {
        Validation<java.time.YearMonth> result = InputRules.month(text, clock);
        if (result instanceof Validation.Invalid<java.time.YearMonth> invalid) {
            errors.showError(invalid.message());
        } else if (result instanceof Validation.Valid<java.time.YearMonth> valid) {
            execute(new ReportCommand.LoadMonth(valid.value()));
        }
    }

    public void branchChosen(String text) {
        Validation<String> result = InputRules.branch(text);
        if (result instanceof Validation.Invalid<String> invalid) {
            errors.showError(invalid.message());
        } else if (result instanceof Validation.Valid<String> valid) {
            execute(new ReportCommand.SelectBranch(valid.value()));
        }
    }

    public void execute(ReportCommand command) {
        try {
            switch (command) {
                case ReportCommand.LoadMonth(var month) -> model.load(dataDir, month);
                case ReportCommand.SelectBranch(var branch) -> model.selectBranch(branch);
            }
            errors.clearError();
        } catch (NoSuchFileException e) {
            String pathStr = e.getFile() != null ? e.getFile() : dataDir.resolve(e.getMessage()).toString();
            pathStr = pathStr.replace('\\', '/');
            errors.showError("No sales data found in " + pathStr);
        } catch (IOException e) {
            errors.showError("Failed to load sales data: " + e.getMessage());
        }
    }
}

