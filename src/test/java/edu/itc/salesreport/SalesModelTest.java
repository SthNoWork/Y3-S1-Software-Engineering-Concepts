package edu.itc.salesreport;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesModelTest {

    @Test
    void successfulLoadFiresMonthLoadedWithExactCountsAndChainRevenue(@TempDir Path tempDir) throws IOException {
        Path dataDir = TestData.createTestDataset(tempDir);
        SalesModel model = new SalesModel();

        List<SalesEvent> events = new ArrayList<>();
        model.addListener(events::add);

        YearMonth month = YearMonth.of(2026, 9);
        model.load(dataDir, month);

        assertEquals(1, events.size());
        SalesEvent event = events.get(0);
        if (event instanceof SalesEvent.MonthLoaded ml) {
            assertEquals(month, ml.month());
            assertEquals(3, ml.rows());
            assertEquals(1, ml.rejected());
        } else {
            throw new AssertionError("Expected MonthLoaded event, got " + event);
        }

        BranchSummary summary = model.summary();
        assertEquals(new BigDecimal("52.00"), summary.totalRevenue());
        assertEquals(3, summary.receiptCount());
        assertEquals(1, summary.rejectedCount());
    }

    @Test
    void selectingSameBranchTwiceFiresEventOnlyOnce() {
        SalesModel model = new SalesModel();
        List<SalesEvent> events = new ArrayList<>();
        model.addListener(events::add);

        model.selectBranch("PNH");
        assertEquals(1, events.size());
        assertEquals(new SalesEvent.BranchSelected("PNH"), events.get(0));

        model.selectBranch("PNH");
        assertEquals(1, events.size(), "Selecting the same branch twice must not fire duplicate events");
    }

    @Test
    void loadingNonExistentMonthThrowsNoSuchFileExceptionFiresNothingAndPreservesState(@TempDir Path tempDir) {
        SalesModel model = new SalesModel();
        List<SalesEvent> events = new ArrayList<>();
        model.addListener(events::add);

        YearMonth nonExistent = YearMonth.of(2020, 1);
        assertThrows(NoSuchFileException.class, () -> model.load(tempDir, nonExistent));

        assertEquals(0, events.size(), "No events should fire on failure");
        assertNull(model.data(), "State must remain unchanged/preserved");
    }

    @Test
    void deregisteredListenersDoNotReceiveEvents(@TempDir Path tempDir) throws IOException {
        Path dataDir = TestData.createTestDataset(tempDir);
        SalesModel model = new SalesModel();

        List<SalesEvent> events = new ArrayList<>();
        SalesModelListener listener = events::add;

        model.addListener(listener);
        model.removeListener(listener);

        model.load(dataDir, YearMonth.of(2026, 9));
        model.selectBranch("PNH");

        assertEquals(0, events.size(), "Deregistered listener must receive 0 events");
    }
}

