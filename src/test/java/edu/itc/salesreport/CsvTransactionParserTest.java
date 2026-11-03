package edu.itc.salesreport;

import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ingest.InvalidRowException;
import edu.itc.salesreport.model.PaymentMethod;
import edu.itc.salesreport.model.SaleTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvTransactionParserTest {
    private CsvTransactionParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvTransactionParser();
    }

    @Test
    void parsesValidLineCorrectly() {
        String line = "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR";
        SaleTransaction tx = parser.parse(line);

        assertEquals("PNH", tx.branch());
        assertEquals(LocalDate.of(2026, 9, 1), tx.date());
        assertEquals("PNH-000123", tx.receiptNo());
        assertEquals("SKU-1001", tx.sku());
        assertEquals("Jasmine Rice 5kg", tx.productName());
        assertEquals("Grocery", tx.category());
        assertEquals(2, tx.quantity());
        assertEquals(new BigDecimal("6.50"), tx.unitPrice());
        assertEquals(new BigDecimal("0.00"), tx.discount());
        assertEquals(PaymentMethod.KHQR, tx.paymentMethod());
        assertEquals(new BigDecimal("13.00"), tx.lineRevenue());
    }

    @ParameterizedTest(name = "Variant {index}: {0}")
    @ValueSource(strings = {
            "",                                                                             // 1. Empty string
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00",      // 2. Missing field (9 fields)
            "BKK,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR", // 3. Unrecognized branch
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Furniture,2,6.50,0.00,KHQR", // 4. Unrecognized category
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,0,6.50,0.00,KHQR",  // 5. Zero quantity
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,-2,6.50,0.00,KHQR", // 6. Negative quantity
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,-6.50,0.00,KHQR", // 7. Negative unit price
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,-0.10,KHQR", // 8. Negative discount
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,1,6.50,7.00,KHQR",  // 9. Discount exceeds gross revenue
            "PNH,not-a-date,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,KHQR",  // 10. Unparseable date
            "PNH,2026-09-01,PNH-000123,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,BITCOIN" // 11. Invalid payment method
    })
    void corruptVariantsThrowInvalidRowException(String corruptLine) {
        assertThrows(InvalidRowException.class, () -> parser.parse(corruptLine));
    }
}

