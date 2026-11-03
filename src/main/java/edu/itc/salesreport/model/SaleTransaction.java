package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record SaleTransaction(
        String branch,
        LocalDate date,
        String receiptNo,
        String sku,
        String productName,
        String category,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discount,
        PaymentMethod paymentMethod
) {
    public BigDecimal lineRevenue() {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return gross.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }
}

