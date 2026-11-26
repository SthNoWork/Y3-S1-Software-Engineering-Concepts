package edu.itc.salesreport.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Plain JUnit: no JavaFX toolkit, no window, no robot, no sleep. */
class ReportViewModelTest {
    static final Clock SEP_27 = Clock.fixed(
            Instant.parse("2026-09-27T03:00:00Z"),
            ZoneId.of("Asia/Phnom_Penh"));
    static final YearMonth AUG = YearMonth.of(2026, 8);

    ManualJobs jobs = new ManualJobs();
    List<String> loaded = new ArrayList<>();
    boolean failNext;
    ReportViewModel vm;

    @BeforeEach
    void setUp() {
        SummarySource source = (month, branch) -> {
            if (failNext) {
                throw new IOException(branch + "-2026-08-03.csv: unreadable");
            }
            loaded.add(branch);
            return summary(branch, "200.00");
        };
        vm = new ReportViewModel(source, jobs, SEP_27);
    }

    static BranchSummary summary(String branch, String revenue) {
        var rice = new ProductTotal("SKU-1001", "Jasmine Rice 5kg", "Grocery", 5, new BigDecimal(revenue));
        var water = new ProductTotal("SKU-2001", "Mineral Water 1.5L", "Beverages", 10, new BigDecimal("50.00"));
        return new BranchSummary(
                branch,
                new BigDecimal(revenue),
                BigDecimal.ZERO,
                10,
                new BigDecimal("20.00"),
                Map.of("Grocery", new BigDecimal(revenue)),
                Map.of(PaymentMethod.CASH, new BigDecimal(revenue)),
                List.of(rice, water),
                0
        );
    }

    @Test
    void cannotGenerateBeforeAMonthIsChosen() {
        assertEquals("Choose a month.", vm.monthErrorProperty().get());
        assertEquals("Choose a month.", vm.validationMessageProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void futureMonthIsRejected() {
        vm.monthProperty().set(YearMonth.of(2026, 10));
        assertEquals("Month 2026-10 is in the future.", vm.monthErrorProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void emptyBranchSelectionIsRejected() {
        vm.monthProperty().set(AUG);
        for (String b : ReportViewModel.BRANCHES) {
            vm.branchSelectedProperty(b).set(false);
        }
        assertEquals("Select at least one branch.", vm.branchErrorProperty().get());
        assertEquals("Select at least one branch.", vm.validationMessageProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
    }

    @Test
    void combinedValidationMessagesJoinWithSpace() {
        for (String b : ReportViewModel.BRANCHES) {
            vm.branchSelectedProperty(b).set(false);
        }
        assertEquals("Choose a month. Select at least one branch.", vm.validationMessageProperty().get());
    }

    @Test
    void generateIsIgnoredWhileInvalid() {
        vm.generateCommand().execute();
        assertFalse(vm.loadingProperty().get());
        assertEquals(0, jobs.pendingCount());
    }

    @Test
    void generateStartsLoadingAndDisablesItself() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();

        assertTrue(vm.loadingProperty().get());
        assertFalse(vm.generateCommand().isExecutable());
        assertTrue(vm.cancelCommand().isExecutable());
        assertEquals(1, jobs.pendingCount());
    }

    @Test
    void successfulLoadFillsSummariesAndTotal() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();

        assertFalse(vm.loadingProperty().get());
        assertEquals(3, vm.summaries().size());
        assertEquals(new BigDecimal("600.00"), vm.totalRevenueProperty().get());
        assertEquals("USD 600.00", vm.totalRevenueTextProperty().get());
        assertEquals("Loaded 3 branch(es) for 2026-08.", vm.statusProperty().get());
        assertEquals("", vm.loadErrorProperty().get());
    }

    @Test
    void progressFollowsTheBranches() {
        List<Double> progressEvents = new ArrayList<>();
        vm.progressProperty().addListener((obs, o, n) -> progressEvents.add(n.doubleValue()));

        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();

        assertTrue(progressEvents.contains(1.0 / 3.0));
        assertTrue(progressEvents.contains(2.0 / 3.0));
        assertTrue(progressEvents.contains(1.0));
    }

    @Test
    void failureKeepsPreviousDataAndShowsTheError() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(3, vm.summaries().size());

        failNext = true;
        vm.generateCommand().execute();
        jobs.runNext();

        assertFalse(vm.loadingProperty().get());
        assertEquals(3, vm.summaries().size());
        assertEquals("USD 600.00", vm.totalRevenueTextProperty().get());
        assertEquals("PNH-2026-08-03.csv: unreadable", vm.loadErrorProperty().get());
        assertEquals("Load failed, previous data kept.", vm.statusProperty().get());
    }

    @Test
    void cancelReenablesGenerateAndKeepsOldData() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();
        assertEquals(3, vm.summaries().size());

        vm.branchSelectedProperty("BTB").set(false);
        vm.generateCommand().execute();
        assertTrue(vm.loadingProperty().get());

        vm.cancelCommand().execute();

        assertFalse(vm.loadingProperty().get());
        assertTrue(vm.generateCommand().isExecutable());
        assertFalse(vm.cancelCommand().isExecutable());
        assertEquals(3, vm.summaries().size());
        assertEquals("Cancelled, previous data kept.", vm.statusProperty().get());
    }

    @Test
    void productFilterIsCaseInsensitive() {
        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();

        assertEquals(2, vm.visibleProducts().size());

        vm.productFilterProperty().set("jasmine");
        assertEquals(1, vm.visibleProducts().size());
        assertEquals("SKU-1001", vm.visibleProducts().get(0).sku());

        vm.productFilterProperty().set("sku-2001");
        assertEquals(1, vm.visibleProducts().size());
        assertEquals("Mineral Water 1.5L", vm.visibleProducts().get(0).productName());
    }

    @Test
    void workerUsesASnapshotOfTheInputs() {
        vm.monthProperty().set(AUG);
        vm.branchSelectedProperty("BTB").set(false);
        vm.generateCommand().execute();

        // Mutate inputs while job is queued
        vm.monthProperty().set(YearMonth.of(2026, 7));
        vm.branchSelectedProperty("BTB").set(true);

        jobs.runNext();
        assertEquals(List.of("PNH", "REP"), loaded);
        assertEquals("Loaded 2 branch(es) for 2026-08.", vm.statusProperty().get());
    }

    @Test
    void twoListenersReceiveSameTotalRevenueForSecondView() {
        List<String> viewerObserved = new ArrayList<>();
        List<String> dashboardObserved = new ArrayList<>();

        vm.totalRevenueTextProperty().addListener((obs, o, n) -> viewerObserved.add(n));
        vm.totalRevenueTextProperty().addListener((obs, o, n) -> dashboardObserved.add(n));

        vm.monthProperty().set(AUG);
        vm.generateCommand().execute();
        jobs.runNext();

        assertTrue(viewerObserved.contains("USD 600.00"));
        assertTrue(dashboardObserved.contains("USD 600.00"));
    }
}
