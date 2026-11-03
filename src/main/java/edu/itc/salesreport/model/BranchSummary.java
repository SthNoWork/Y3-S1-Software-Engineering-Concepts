package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record BranchSummary(
        String branch,
        BigDecimal totalRevenue,
        BigDecimal totalDiscount,
        int receiptCount,
        BigDecimal averageBasket,
        Map<String, BigDecimal> revenueByCategory,
        Map<PaymentMethod, BigDecimal> revenueByPaymentMethod,
        List<ProductTotal> topProducts,
        int rejectedCount
) {}

