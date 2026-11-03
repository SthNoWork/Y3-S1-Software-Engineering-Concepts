package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.ProductTotal;
import edu.itc.salesreport.model.SaleTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class DefaultSalesAggregator implements SalesAggregator {

    @Override
    public BranchSummary aggregate(String branch, List<SaleTransaction> transactions, int rejectedCount) {
        String filterBranch = (branch == null || branch.isBlank()) ? "ALL" : branch.trim().toUpperCase();

        List<SaleTransaction> filtered;
        if ("ALL".equalsIgnoreCase(filterBranch)) {
            filtered = transactions != null ? transactions : List.of();
        } else {
            filtered = transactions != null
                    ? transactions.stream().filter(t -> t.branch().equalsIgnoreCase(filterBranch)).toList()
                    : List.of();
        }

        BigDecimal totalRevenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDiscount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        Map<String, BigDecimal> revenueByCategory = new TreeMap<>();
        Map<PaymentMethod, BigDecimal> revenueByPaymentMethod = new EnumMap<>(PaymentMethod.class);

        for (PaymentMethod pm : PaymentMethod.values()) {
            revenueByPaymentMethod.put(pm, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        Map<String, ProductAccumulator> productMap = new HashMap<>();
        long distinctReceipts = filtered.stream()
                .map(SaleTransaction::receiptNo)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        for (SaleTransaction tx : filtered) {
            BigDecimal lineRev = tx.lineRevenue();
            totalRevenue = totalRevenue.add(lineRev);
            totalDiscount = totalDiscount.add(tx.discount());

            revenueByCategory.merge(tx.category(), lineRev, BigDecimal::add);
            revenueByPaymentMethod.merge(tx.paymentMethod(), lineRev, BigDecimal::add);

            productMap.computeIfAbsent(tx.sku(), k -> new ProductAccumulator(tx.sku(), tx.productName(), tx.category()))
                    .accumulate(tx.quantity(), lineRev);
        }

        BigDecimal averageBasket = distinctReceipts == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : totalRevenue.divide(BigDecimal.valueOf(distinctReceipts), 2, RoundingMode.HALF_UP);

        List<ProductTotal> topProducts = productMap.values().stream()
                .map(ProductAccumulator::toProductTotal)
                .sorted(Comparator.comparing(ProductTotal::totalRevenue).reversed()
                        .thenComparing(ProductTotal::sku))
                .limit(10)
                .toList();

        return new BranchSummary(
                filterBranch,
                totalRevenue.setScale(2, RoundingMode.HALF_UP),
                totalDiscount.setScale(2, RoundingMode.HALF_UP),
                (int) distinctReceipts,
                averageBasket,
                revenueByCategory,
                revenueByPaymentMethod,
                topProducts,
                rejectedCount
        );
    }

    private static final class ProductAccumulator {
        private final String sku;
        private final String productName;
        private final String category;
        private int quantity;
        private BigDecimal revenue = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        ProductAccumulator(String sku, String productName, String category) {
            this.sku = sku;
            this.productName = productName;
            this.category = category;
        }

        void accumulate(int qty, BigDecimal rev) {
            this.quantity += qty;
            this.revenue = this.revenue.add(rev);
        }

        ProductTotal toProductTotal() {
            return new ProductTotal(sku, productName, category, quantity, revenue);
        }
    }
}

