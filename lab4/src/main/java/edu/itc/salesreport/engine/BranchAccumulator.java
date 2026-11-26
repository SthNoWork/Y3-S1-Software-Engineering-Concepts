package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import edu.itc.salesreport.model.SaleTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Running totals of one branch. NOT thread-safe: only one thread may use it at a time. */
public final class BranchAccumulator {
    static final BigDecimal ZERO = new BigDecimal("0.00");

    private BigDecimal revenue = ZERO;
    private BigDecimal discounts = ZERO;
    // receipt numbers restart every day, so they are counted per day
    private final Map<LocalDate, Set<String>> receipts = new HashMap<>();
    private final Map<String, BigDecimal> byCategory = new TreeMap<>();
    private final Map<PaymentMethod, BigDecimal> byPayment = new EnumMap<>(PaymentMethod.class);
    private final Map<String, ProductTotal> bySku = new HashMap<>();
    private final Map<LocalDate, BigDecimal> byDay = new TreeMap<>();

    public void add(SaleTransaction tx) {
        BigDecimal r = tx.revenue();
        revenue = revenue.add(r);
        discounts = discounts.add(tx.discount());
        receipts.computeIfAbsent(tx.date(), d -> new HashSet<>()).add(tx.receiptNo());
        byCategory.merge(tx.category(), r, BigDecimal::add);
        byPayment.merge(tx.paymentMethod(), r, BigDecimal::add);
        bySku.merge(tx.sku(), new ProductTotal(tx.sku(), tx.productName(), r, tx.quantity()),
                BranchAccumulator::sum);
        byDay.merge(tx.date(), r, BigDecimal::add);
    }

    private static ProductTotal sum(ProductTotal a, ProductTotal b) {
        return new ProductTotal(a.sku(), a.productName(),
                a.revenue().add(b.revenue()), a.quantity() + b.quantity());
    }

    /** Adds the totals of other into this one. */
    public void merge(BranchAccumulator other) {
        revenue = revenue.add(other.revenue);
        discounts = discounts.add(other.discounts);
        other.receipts.forEach((day, set) ->
                receipts.computeIfAbsent(day, d -> new HashSet<>()).addAll(set));
        other.byCategory.forEach((k, v) -> byCategory.merge(k, v, BigDecimal::add));
        other.byPayment.forEach((k, v) -> byPayment.merge(k, v, BigDecimal::add));
        other.bySku.forEach((k, v) -> bySku.merge(k, v, BranchAccumulator::sum));
        other.byDay.forEach((k, v) -> byDay.merge(k, v, BigDecimal::add));
    }

    public Map<LocalDate, BigDecimal> dailyRevenue() {
        return Map.copyOf(byDay);
    }

    /** An immutable summary: safe to give to other threads. */
    public BranchSummary toSummary(String branch) {
        List<ProductTotal> top = bySku.values().stream()
                .sorted(Comparator.comparing(ProductTotal::revenue).reversed()
                        .thenComparing(ProductTotal::sku))    // same order on every run
                .limit(10)
                .toList();
        long receiptCount = 0;
        for (Set<String> perDay : receipts.values()) {
            receiptCount += perDay.size();
        }
        return new BranchSummary(branch, revenue, discounts, receiptCount,
                byCategory, top, byPayment);
    }
}
