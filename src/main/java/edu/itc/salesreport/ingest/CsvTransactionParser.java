package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class CsvTransactionParser implements TransactionParser {
    private static final Set<String> VALID_BRANCHES = Set.of("PNH", "REP", "BTB");
    private static final Set<String> VALID_CATEGORIES = Set.of(
            "Grocery", "Beverages", "Household", "Personal Care", "Electronics", "Stationery"
    );

    @Override
    public SaleTransaction parse(String line) throws InvalidRowException {
        if (line == null || line.isBlank()) {
            throw new InvalidRowException("Line is empty or null");
        }

        String[] parts = line.split(",", -1);
        if (parts.length != 10) {
            throw new InvalidRowException("Expected 10 comma-separated fields, got " + parts.length);
        }

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
            if (parts[i].isEmpty()) {
                throw new InvalidRowException("Field " + i + " is empty");
            }
        }

        String branch = parts[0];
        if (!VALID_BRANCHES.contains(branch)) {
            throw new InvalidRowException("Unrecognized branch: " + branch);
        }

        LocalDate date;
        try {
            date = LocalDate.parse(parts[1]);
        } catch (DateTimeParseException e) {
            throw new InvalidRowException("Invalid date format: " + parts[1], e);
        }

        String receiptNo = parts[2];
        String sku = parts[3];
        String productName = parts[4];

        String category = parts[5];
        if (!VALID_CATEGORIES.contains(category)) {
            throw new InvalidRowException("Unrecognized category: " + category);
        }

        int quantity;
        try {
            quantity = Integer.parseInt(parts[6]);
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid quantity integer: " + parts[6], e);
        }
        if (quantity <= 0) {
            throw new InvalidRowException("Quantity must be positive: " + quantity);
        }

        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(parts[7]);
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid unit price: " + parts[7], e);
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRowException("Unit price cannot be negative: " + unitPrice);
        }

        BigDecimal discount;
        try {
            discount = new BigDecimal(parts[8]);
        } catch (NumberFormatException e) {
            throw new InvalidRowException("Invalid discount: " + parts[8], e);
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRowException("Discount cannot be negative: " + discount);
        }

        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (discount.compareTo(gross) > 0) {
            throw new InvalidRowException("Discount (" + discount + ") exceeds gross revenue (" + gross + ")");
        }

        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(parts[9]);
        } catch (IllegalArgumentException e) {
            throw new InvalidRowException("Invalid payment method: " + parts[9], e);
        }

        return new SaleTransaction(
                branch, date, receiptNo, sku, productName, category,
                quantity, unitPrice, discount, paymentMethod
        );
    }
}

