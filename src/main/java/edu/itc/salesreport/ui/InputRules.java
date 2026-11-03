package edu.itc.salesreport.ui;

import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;

public final class InputRules {
    private static final Set<String> VALID_BRANCHES = Set.of("ALL", "PNH", "REP", "BTB");

    private InputRules() {}

    public static Validation<YearMonth> month(String text, Clock clock) {
        if (text == null || text.isBlank()) {
            return new Validation.Invalid<>("Enter a month as yyyy-MM, e.g. 2026-09");
        }

        String trimmed = text.trim();
        YearMonth parsed;
        try {
            parsed = YearMonth.parse(trimmed);
        } catch (DateTimeParseException e) {
            return new Validation.Invalid<>("'" + trimmed + "' is not a month in the form yyyy-MM");
        }

        YearMonth now = YearMonth.now(clock);
        if (parsed.isAfter(now)) {
            return new Validation.Invalid<>(parsed + " is in the future");
        }

        return new Validation.Valid<>(parsed);
    }

    public static Validation<String> branch(String text) {
        if (text == null || text.isBlank()) {
            return new Validation.Invalid<>("Branch cannot be empty");
        }

        String upper = text.trim().toUpperCase(Locale.ROOT);
        if (!VALID_BRANCHES.contains(upper)) {
            return new Validation.Invalid<>("Unrecognized branch: " + text.trim());
        }

        return new Validation.Valid<>(upper);
    }
}

