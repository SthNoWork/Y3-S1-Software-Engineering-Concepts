package edu.itc.salesreport.ui.mvp;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportPresenter implements SalesModelListener, ErrorDisplay {
    private final SalesModel model;
    private final ReportScreen screen;
    private final ReportController controller;

    public ReportPresenter(SalesModel model, ReportScreen screen, ReportController controller) {
        this.model = model;
        this.screen = screen;
        this.controller = controller;
    }

    @Override
    public void modelChanged(SalesEvent event) {
        List<SaleTransaction> transactions = model.visibleTransactions();
        List<RowItem> rows = new ArrayList<>(transactions.size());
        for (SaleTransaction tx : transactions) {
            rows.add(new RowItem(
                    tx.date().toString(),
                    tx.receiptNo(),
                    tx.productName(),
                    tx.category(),
                    String.valueOf(tx.quantity()),
                    String.format(Locale.US, "%,.2f", tx.lineRevenue()),
                    String.format(Locale.US, "%,.2f", tx.discount())
            ));
        }
        screen.showRows(rows);

        BranchSummary summary = model.summary();
        screen.showCategoryRevenue(summary.revenueByCategory());

        String status;
        if (event instanceof SalesEvent.MonthLoaded ml) {
            status = String.format(Locale.US,
                    "Loaded %s: %d rows, %d rejected | Branch: %s | Revenue: $%s | Receipts: %d | Avg Basket: $%s",
                    ml.month(), ml.rows(), ml.rejected(), summary.branch(),
                    String.format(Locale.US, "%,.2f", summary.totalRevenue()),
                    summary.receiptCount(),
                    String.format(Locale.US, "%,.2f", summary.averageBasket())
            );
        } else {
            status = String.format(Locale.US,
                    "Branch: %s | Revenue: $%s | Receipts: %d | Avg Basket: $%s | Rejected: %d",
                    summary.branch(),
                    String.format(Locale.US, "%,.2f", summary.totalRevenue()),
                    summary.receiptCount(),
                    String.format(Locale.US, "%,.2f", summary.averageBasket()),
                    summary.rejectedCount()
            );
        }
        screen.showStatus(status);
    }

    public void monthEntered(String text) {
        controller.monthEntered(text);
    }

    public void branchChosen(String text) {
        controller.branchChosen(text);
    }

    @Override
    public void showError(String message) {
        screen.showError(message);
    }

    @Override
    public void clearError() {
        screen.showError("");
    }
}

