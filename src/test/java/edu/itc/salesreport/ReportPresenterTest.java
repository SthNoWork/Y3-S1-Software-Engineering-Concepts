package edu.itc.salesreport;

import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.mvp.ReportPresenter;
import edu.itc.salesreport.ui.mvp.ReportScreen;
import edu.itc.salesreport.ui.mvp.RowItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportPresenterTest {

    private static class FakeScreen implements ReportScreen {
        List<RowItem> rows = new ArrayList<>();
        Map<String, BigDecimal> categoryRevenue;
        String status = "";
        String error = "";

        @Override
        public void showRows(List<RowItem> rows) {
            this.rows = List.copyOf(rows);
        }

        @Override
        public void showCategoryRevenue(Map<String, BigDecimal> revenue) {
            this.categoryRevenue = revenue;
        }

        @Override
        public void showStatus(String text) {
            this.status = text;
        }

        @Override
        public void showError(String message) {
            this.error = message;
        }
    }

    @Test
    void presenterFormatsCurrencyWithCommasAndTwoDecimalsWithoutJavaFx(@TempDir Path tempDir) throws IOException {
        Path dataDir = TestData.createTestDataset(tempDir);
        SalesModel model = new SalesModel();
        FakeScreen screen = new FakeScreen();
        ReportController controller = new ReportController(model, dataDir, Clock.systemDefaultZone(), screen::showError);
        ReportPresenter presenter = new ReportPresenter(model, screen, controller);

        model.addListener(presenter);
        model.load(dataDir, YearMonth.of(2026, 9));

        assertFalse(screen.rows.isEmpty());
        RowItem first = screen.rows.get(0);
        assertEquals("13.00", first.revenue());
        assertEquals("0.00", first.discount());

        assertTrue(screen.status.contains("52.00"));
        assertTrue(screen.categoryRevenue.containsKey("Grocery"));
    }

    @Test
    void presenterRoutesBranchSelectionAndUpdatesScreen(@TempDir Path tempDir) throws IOException {
        Path dataDir = TestData.createTestDataset(tempDir);
        SalesModel model = new SalesModel();
        FakeScreen screen = new FakeScreen();
        ReportController controller = new ReportController(model, dataDir, Clock.systemDefaultZone(), screen::showError);
        ReportPresenter presenter = new ReportPresenter(model, screen, controller);

        model.addListener(presenter);
        model.load(dataDir, YearMonth.of(2026, 9));

        presenter.branchChosen("PNH");
        assertEquals("PNH", model.branch());
        assertTrue(screen.status.contains("Branch: PNH"));
    }

    @Test
    void presenterHandlesErrorsViaScreen() {
        SalesModel model = new SalesModel();
        FakeScreen screen = new FakeScreen();
        ReportController controller = new ReportController(model, Path.of("nonexistent"), Clock.systemDefaultZone(), screen::showError);
        ReportPresenter presenter = new ReportPresenter(model, screen, controller);

        presenter.showError("Test error");
        assertEquals("Test error", screen.error);

        presenter.clearError();
        assertEquals("", screen.error);
    }
}

