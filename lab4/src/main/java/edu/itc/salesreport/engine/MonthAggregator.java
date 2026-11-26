package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;
import edu.itc.salesreport.model.SaleTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Totals per branch plus the invalid rows. NOT thread-safe: give every thread
 * its own MonthAggregator and merge them when the threads are done.
 */
public final class MonthAggregator implements SalesAggregator {
    private final Map<String, BranchAccumulator> branches = new TreeMap<>();
    private final List<String> invalid = new ArrayList<>();

    @Override
    public void add(SaleTransaction tx) {
        branches.computeIfAbsent(tx.branch(), b -> new BranchAccumulator()).add(tx);
    }

    @Override
    public void addInvalid(String anomaly) {
        invalid.add("INVALID " + anomaly);
    }

    /** Adds everything of other into this one. Do not use other afterwards. */
    public void merge(MonthAggregator other) {
        other.branches.forEach((branch, acc) ->
                branches.computeIfAbsent(branch, b -> new BranchAccumulator()).merge(acc));
        invalid.addAll(other.invalid);
    }

    /** The summary of one branch, for example "PNH". */
    public BranchSummary summaryOf(String branch) {
        return branches.getOrDefault(branch, new BranchAccumulator()).toSummary(branch);
    }

    @Override
    public MonthlyReport toReport(YearMonth month) {
        List<BranchSummary> summaries = new ArrayList<>();
        BranchAccumulator chain = new BranchAccumulator();
        List<String> anomalies = new ArrayList<>(invalid);
        // TreeMap: the branches come sorted by code (BTB, PNH, REP)
        for (Map.Entry<String, BranchAccumulator> e : branches.entrySet()) {
            summaries.add(e.getValue().toSummary(e.getKey()));
            chain.merge(e.getValue());
            anomalies.addAll(spikes(e.getKey(), e.getValue().dailyRevenue()));
        }
        Collections.sort(anomalies);          // same order whatever thread found them
        return new MonthlyReport(month, summaries, chain.toSummary("CHAIN"), anomalies);
    }

    /** Days whose revenue is more than 3 standard deviations from the branch's daily mean. */
    static List<String> spikes(String branch, Map<LocalDate, BigDecimal> daily) {
        if (daily.size() < 2) return List.of();
        double mean = 0;
        for (BigDecimal v : daily.values()) mean += v.doubleValue();
        mean /= daily.size();
        double variance = 0;
        for (BigDecimal v : daily.values()) {
            variance += (v.doubleValue() - mean) * (v.doubleValue() - mean);
        }
        double sd = Math.sqrt(variance / daily.size());
        List<String> out = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> e : new TreeMap<>(daily).entrySet()) {
            double z = sd == 0 ? 0 : (e.getValue().doubleValue() - mean) / sd;
            if (Math.abs(z) > 3) {
                out.add("SPIKE %s %s revenue %s (z=%.1f)"
                        .formatted(branch, e.getKey(), e.getValue(), z));
            }
        }
        return out;
    }
}
