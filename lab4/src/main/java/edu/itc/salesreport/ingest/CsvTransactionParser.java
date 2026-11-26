package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

/** Stateless, therefore safe to share between threads. */
public final class CsvTransactionParser implements TransactionParser {
    public static final Set<String> CATEGORIES = Set.of("Grocery", "Beverages",
            "Household", "Personal Care", "Electronics", "Stationery");

    @Override
    public SaleTransaction parse(String line) throws InvalidRowException {
        String[] f = line.split(",", -1);
        if (f.length != 10) {
            throw new InvalidRowException("expected 10 fields but found " + f.length);
        }
        try {
            int quantity = Integer.parseInt(f[6]);
            BigDecimal unitPrice = new BigDecimal(f[7]);
            BigDecimal discount = new BigDecimal(f[8]);
            if (quantity <= 0) throw new InvalidRowException("quantity must be > 0");
            if (unitPrice.signum() < 0 || discount.signum() < 0) {
                throw new InvalidRowException("negative price or discount");
            }
            if (!CATEGORIES.contains(f[5])) {
                throw new InvalidRowException("unknown category " + f[5]);
            }
            return new SaleTransaction(f[0], LocalDate.parse(f[1]), f[2], f[3], f[4],
                    f[5], quantity, unitPrice, discount, PaymentMethod.valueOf(f[9]));
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new InvalidRowException("unparsable field: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidRowException("unknown payment method " + f[9]);
        }
    }
}
