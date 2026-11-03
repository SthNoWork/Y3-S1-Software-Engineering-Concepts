package edu.itc.salesreport;

import edu.itc.salesreport.ui.InputRules;
import edu.itc.salesreport.ui.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputRulesTest {
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
                Instant.parse("2026-09-27T03:00:00Z"),
                ZoneId.of("Asia/Phnom_Penh")
        );
    }

    @Test
    void blankMonthReturnsUsageMessage() {
        Validation<YearMonth> res = InputRules.month("   ", fixedClock);
        assertInstanceOf(Validation.Invalid.class, res);
        assertEquals("Enter a month as yyyy-MM, e.g. 2026-09", ((Validation.Invalid<YearMonth>) res).message());
    }

    @Test
    void malformedMonthFormatReturnsDescriptiveError() {
        Validation<YearMonth> res1 = InputRules.month("2026/09", fixedClock);
        assertInstanceOf(Validation.Invalid.class, res1);
        assertEquals("'2026/09' is not a month in the form yyyy-MM", ((Validation.Invalid<YearMonth>) res1).message());

        Validation<YearMonth> res2 = InputRules.month("2026-13", fixedClock);
        assertInstanceOf(Validation.Invalid.class, res2);
        assertEquals("'2026-13' is not a month in the form yyyy-MM", ((Validation.Invalid<YearMonth>) res2).message());
    }

    @Test
    void futureMonthReturnsFutureError() {
        Validation<YearMonth> res = InputRules.month("2026-10", fixedClock);
        assertInstanceOf(Validation.Invalid.class, res);
        assertEquals("2026-10 is in the future", ((Validation.Invalid<YearMonth>) res).message());
    }

    @Test
    void validMonthParsesSuccessfully() {
        Validation<YearMonth> res = InputRules.month("2026-08", fixedClock);
        assertInstanceOf(Validation.Valid.class, res);
        assertEquals(YearMonth.of(2026, 8), ((Validation.Valid<YearMonth>) res).value());

        Validation<YearMonth> current = InputRules.month("2026-09", fixedClock);
        assertInstanceOf(Validation.Valid.class, current);
        assertEquals(YearMonth.of(2026, 9), ((Validation.Valid<YearMonth>) current).value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "all", "PNH", "pnh", "REP", "rep", "BTB", "btb"})
    void validBranchesNormalizeToUpperCase(String input) {
        Validation<String> res = InputRules.branch(input);
        assertInstanceOf(Validation.Valid.class, res);
        assertEquals(input.toUpperCase(), ((Validation.Valid<String>) res).value());
    }

    @Test
    void unrecognizedBranchReturnsError() {
        Validation<String> res = InputRules.branch("XYZ");
        assertInstanceOf(Validation.Invalid.class, res);
        assertTrue(((Validation.Invalid<String>) res).message().contains("Unrecognized branch: XYZ"));
    }

    @Test
    void emptyBranchReturnsError() {
        Validation<String> res = InputRules.branch("");
        assertInstanceOf(Validation.Invalid.class, res);
        assertEquals("Branch cannot be empty", ((Validation.Invalid<String>) res).message());
    }
}

