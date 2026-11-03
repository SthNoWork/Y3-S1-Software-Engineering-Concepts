package edu.itc.salesreport.model;

import java.math.BigDecimal;

public record ProductTotal(
        String sku,
        String productName,
        String category,
        int totalQuantity,
        BigDecimal totalRevenue
) {}

