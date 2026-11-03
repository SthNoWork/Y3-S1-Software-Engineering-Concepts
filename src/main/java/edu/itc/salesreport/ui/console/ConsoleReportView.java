package edu.itc.salesreport.ui.console;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.Locale;

public class ConsoleReportView implements SalesModelListener {
    private final SalesModel model;

    public ConsoleReportView(SalesModel model) {
        this.model = model;
    }

    @Override
    public void modelChanged(SalesEvent event) {
        BranchSummary summary = model.summary();
        System.out.printf(Locale.US,
                "[%s] Branch: %s | Revenue: %.2f | Receipts: %d | Basket: %.2f | Rejected: %d%n",
                event.getClass().getSimpleName(),
                summary.branch(),
                summary.totalRevenue(),
                summary.receiptCount(),
                summary.averageBasket(),
                summary.rejectedCount()
        );
    }

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of(args.length > 0 ? args[0] : "data");
        SalesModel model = new SalesModel();
        ConsoleReportView view = new ConsoleReportView(model);
        model.addListener(view);

        YearMonth month = YearMonth.of(2026, 9);
        model.load(dataDir, month);
        model.selectBranch("ALL");
        model.selectBranch("PNH");
    }
}

