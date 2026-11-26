package edu.itc.salesreport.ingest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.SplittableRandom;

/** Seeded generator: the same (month, rowsPerDay, seed) must give identical bytes. */
public final class SalesDataGenerator {

    public static final List<String> BRANCHES = List.of("PNH", "REP", "BTB");
    private static final String[] PAYMENTS = {"CASH", "CARD", "KHQR"};

    record Product(String sku, String name, String category, String price) {}

    static final List<Product> CATALOG = List.of(
        new Product("SKU-1001", "Jasmine Rice 5kg", "Grocery", "6.50"),
        new Product("SKU-1002", "Fish Sauce 700ml", "Grocery", "1.80"),
        new Product("SKU-1003", "Palm Sugar 1kg", "Grocery", "2.40"),
        new Product("SKU-1004", "Instant Noodles x5", "Grocery", "1.25"),
        new Product("SKU-2001", "Bottled Water 1.5L", "Beverages", "0.60"),
        new Product("SKU-2002", "Iced Coffee Can", "Beverages", "0.90"),
        new Product("SKU-2003", "Orange Juice 1L", "Beverages", "2.20"),
        new Product("SKU-2004", "Green Tea 500ml", "Beverages", "0.75"),
        new Product("SKU-3001", "Laundry Powder 2kg", "Household", "4.90"),
        new Product("SKU-3002", "Dish Soap 800ml", "Household", "1.60"),
        new Product("SKU-3003", "Mosquito Coil x10", "Household", "1.10"),
        new Product("SKU-4001", "Shampoo 400ml", "Personal Care", "3.80"),
        new Product("SKU-4002", "Toothpaste 150g", "Personal Care", "1.95"),
        new Product("SKU-4003", "Sunscreen SPF50", "Personal Care", "7.50"),
        new Product("SKU-5001", "USB-C Cable 1m", "Electronics", "3.50"),
        new Product("SKU-5002", "AA Batteries x4", "Electronics", "2.80"),
        new Product("SKU-5003", "LED Bulb 9W", "Electronics", "2.10"),
        new Product("SKU-5004", "Power Bank 10000mAh", "Electronics", "18.90"),
        new Product("SKU-6001", "Notebook A5", "Stationery", "0.85"),
        new Product("SKU-6002", "Ballpoint Pen x10", "Stationery", "1.50"));

    /** Usage: java -cp target/classes edu.itc.salesreport.ingest.SalesDataGenerator 2026-09 5000 42 data */
    public static void main(String[] args) throws IOException {
        new SalesDataGenerator().generate(YearMonth.parse(args[0]),
                Integer.parseInt(args[1]), Long.parseLong(args[2]), Path.of(args[3]));
    }

    public void generate(YearMonth month, int rowsPerDay, long seed, Path dataDir)
            throws IOException {
        Path dir = Files.createDirectories(dataDir.resolve(month.toString()));
        for (String branch : BRANCHES) {
            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                LocalDate date = month.atDay(day);
                writeFile(dir.resolve(branch + "-" + date + ".csv"), branch, date,
                        rowsPerDay, fileSeed(seed, branch, date));
            }
        }
    }

    /** One independent stream per file, so files can be generated in any order. */
    static long fileSeed(long seed, String branch, LocalDate date) {
        return seed * 1_000_003L + branch.hashCode() * 10_007L + date.toEpochDay();
    }

    private void writeFile(Path file, String branch, LocalDate date, int rows,
            long seed) throws IOException {
        var rnd = new SplittableRandom(seed);
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(TransactionParser.HEADER);
            w.write('\n');                      // LF on every OS: identical bytes
            int receipt = 0, linesLeft = 0;
            String payment = "CASH";
            for (int i = 0; i < rows; i++) {
                // keep this order of random calls, or your data will differ
                if (linesLeft == 0) {
                    receipt++;
                    linesLeft = 1 + rnd.nextInt(5);
                    payment = PAYMENTS[rnd.nextInt(PAYMENTS.length)];
                }
                linesLeft--;
                Product p = CATALOG.get(rnd.nextInt(CATALOG.size()));
                int qty = 1 + rnd.nextInt(4);
                BigDecimal gross = new BigDecimal(p.price()).multiply(BigDecimal.valueOf(qty));
                BigDecimal discount = rnd.nextInt(10) == 0
                        ? gross.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP)
                        : new BigDecimal("0.00");
                // one canonical row, for example
                //   PNH,2026-09-01,PNH-000001,SKU-1001,Jasmine Rice 5kg,Grocery,2,6.50,0.00,CARD
                w.write(String.join(",", branch, date.toString(),
                        "%s-%06d".formatted(branch, receipt), p.sku(), p.name(), p.category(),
                        Integer.toString(qty), p.price(), discount.toPlainString(), payment));
                w.write('\n');
            }
        }
    }
}
