package edu.itc.salesreport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;

public final class TestData {
    private TestData() {}

    /**
     * Creates a test data directory with sample transactions for 2026-09 whose
     * valid row line revenues sum to exactly 52.00:
     * - Jasmine Rice: 2 * 6.50 = 13.00
     * - Power Bank: 2 * 18.00 = 36.00
     * - Notebook: 4 * 0.75 = 3.00
     * Total = 52.00
     * Plus 1 corrupted row.
     */
    public static Path createTestDataset(Path tempDir) throws IOException {
        YearMonth month = YearMonth.of(2026, 9);
        Path monthDir = tempDir.resolve(month.toString());
        Files.createDirectories(monthDir);

        Path pnhFile = monthDir.resolve("PNH-2026-09-01.csv");
        List<String> lines = List.of(
                "branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method",
                "PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,CASH",
                "PNH,2026-09-01,PNH-000002,SKU-5002,Power Bank 10000mAh,Electronics,2,18.00,0.00,KHQR",
                "PNH,2026-09-01,PNH-000003,SKU-6001,Notebook A5,Stationery,4,0.75,0.00,CARD",
                "PNH,2026-09-01,PNH-999999,SKU-1001,Jasmine Rice 5kg,Grocery,-1,6.50,0.00,KHQR"
        );
        Files.write(pnhFile, lines, StandardCharsets.UTF_8);

        return tempDir;
    }
}

