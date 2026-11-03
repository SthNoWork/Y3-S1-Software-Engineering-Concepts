package edu.itc.salesreport;

import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportControllerTest {
    private SalesModel model;
    private FakeErrors errors;
    private Clock fixedClock;

    private static class FakeErrors implements ErrorDisplay {
        String lastError = "";
        int clearCount = 0;
        int showCount = 0;

        @Override
        public void showError(String message) {
            this.lastError = message;
            this.showCount++;
        }

        @Override
        public void clearError() {
            this.lastError = "";
            this.clearCount++;
        }
    }

    @BeforeEach
    void setUp() {
        model = new SalesModel();
        errors = new FakeErrors();
        fixedClock = Clock.fixed(
                Instant.parse("2026-09-27T03:00:00Z"),
                ZoneId.of("Asia/Phnom_Penh")
        );
    }

    @Test
    void monthEnteredValidLoadsMonthAndClearsError(@TempDir Path tempDir) throws IOException {
        Path dataDir = TestData.createTestDataset(tempDir);
        ReportController controller = new ReportController(model, dataDir, fixedClock, errors);

        controller.monthEntered("2026-09");

        assertEquals("", errors.lastError);
        assertTrue(errors.clearCount > 0);
        assertEquals(YearMonth.of(2026, 9), model.currentMonth());
        assertEquals(3, model.data().rows().size());
    }

    @Test
    void monthEnteredInvalidFormatShowsErrorAndKeepsModelUnchanged(@TempDir Path tempDir) {
        ReportController controller = new ReportController(model, tempDir, fixedClock, errors);

        controller.monthEntered("2026/09");

        assertEquals("'2026/09' is not a month in the form yyyy-MM", errors.lastError);
        assertNull(model.currentMonth());
    }

    @Test
    void monthEnteredNonExistentDirectoryShowsNoSalesDataFound(@TempDir Path tempDir) {
        ReportController controller = new ReportController(model, tempDir, fixedClock, errors);

        controller.monthEntered("2026-08");

        assertTrue(errors.lastError.startsWith("No sales data found in "), "Expected error prefix, got: " + errors.lastError);
        assertNull(model.currentMonth());
    }

    @Test
    void branchChosenValidUpdatesModelAndClearsError(@TempDir Path tempDir) {
        ReportController controller = new ReportController(model, tempDir, fixedClock, errors);

        controller.branchChosen("pnh");

        assertEquals("PNH", model.branch());
        assertEquals("", errors.lastError);
    }

    @Test
    void branchChosenInvalidShowsErrorAndPreservesBranch(@TempDir Path tempDir) {
        ReportController controller = new ReportController(model, tempDir, fixedClock, errors);

        controller.branchChosen("XYZ");

        assertTrue(errors.lastError.contains("Unrecognized branch: XYZ"));
        assertEquals("ALL", model.branch());
    }
}

