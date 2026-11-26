package edu.itc.salesreport.ingest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class SalesDataGenerator {
    private record ProductDef(String sku, String name, String category, double price) {}

    private static final List<ProductDef> PRODUCTS = List.of(
            new ProductDef("SKU-1001", "Jasmine Rice 5kg", "Grocery", 6.50),
            new ProductDef("SKU-1002", "Fish Sauce 700ml", "Grocery", 1.80),
            new ProductDef("SKU-2001", "Mineral Water 1.5L", "Beverages", 0.45),
            new ProductDef("SKU-2002", "Iced Coffee Can", "Beverages", 0.90),
            new ProductDef("SKU-3001", "Dish Soap 500ml", "Household", 1.25),
            new ProductDef("SKU-3002", "Laundry Powder 1kg", "Household", 2.95),
            new ProductDef("SKU-4001", "Toothpaste 150g", "Personal Care", 1.60),
            new ProductDef("SKU-4002", "Shampoo 400ml", "Personal Care", 3.40),
            new ProductDef("SKU-5001", "USB-C Cable 1m", "Electronics", 4.50),
            new ProductDef("SKU-5002", "Power Bank 10000mAh", "Electronics", 18.00),
            new ProductDef("SKU-6001", "Notebook A5", "Stationery", 0.75),
            new ProductDef("SKU-6002", "Ballpoint Pen Blue", "Stationery", 0.30)
    );

    private static final List<String> PAYMENTS = List.of("CASH", "CARD", "KHQR");

    public static void main(String[] args) throws IOException {
        if (args.length >= 3) {
            YearMonth month = YearMonth.parse(args[0]);
            int rowsPerDay = Integer.parseInt(args[1]);
            long seed = Long.parseLong(args[2]);
            Path baseDir = Path.of(args.length > 3 ? args[3] : "data");
            generateCustom(baseDir, month, rowsPerDay, seed);
            return;
        }
        Path baseDir = Path.of(args.length > 0 ? args[0] : "data");
        generate(baseDir);
    }

    public static void generateCustom(Path baseDir, YearMonth month, int rowsPerDay, long seed) throws IOException {
        Path dir = baseDir.resolve(month.toString());
        Files.createDirectories(dir);
        Random random = new Random(seed);
        int days = month.lengthOfMonth();
        String[] payments = {"CASH", "CARD", "KHQR", "KHQR"};

        for (String branch : List.of("PNH", "REP", "BTB")) {
            int receipt = 0;
            for (int d = 1; d <= days; d++) {
                String dateStr = month.atDay(d).toString();
                String filename = branch + "-" + dateStr + ".csv";
                try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve(filename), StandardCharsets.UTF_8)) {
                    writer.write("branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method\n");
                    String payment = "CASH";
                    for (int r = 0; r < rowsPerDay; r++) {
                        if (r == 0 || random.nextInt(3) == 0) {
                            receipt++;
                            payment = payments[random.nextInt(payments.length)];
                        }
                        ProductDef p = PRODUCTS.get(random.nextInt(PRODUCTS.size()));
                        int qty = 1 + random.nextInt(4);
                        String discount = random.nextInt(10) == 0 ? "0.10" : "0.00";
                        writer.write(String.join(",",
                                branch,
                                dateStr,
                                String.format("%s-%06d", branch, receipt),
                                p.sku(),
                                p.name(),
                                p.category(),
                                String.valueOf(qty),
                                String.format(Locale.US, "%.2f", p.price()),
                                discount,
                                payment));
                        writer.write("\n");
                    }
                }
            }
        }
    }

    public static void generate(Path baseDir) throws IOException {
        generateMonth(baseDir, "2026-08", 31, Map.of(
                "PNH", new BranchConfig(500, 900000),
                "REP", new BranchConfig(500, 900000),
                "BTB", new BranchConfig(500, 900000)
        ));

        generateMonth(baseDir, "2026-09", 30, Map.of(
                "PNH", new BranchConfig(489, 845590),
                "REP", new BranchConfig(502, 913300),
                "BTB", new BranchConfig(502, 913300)
        ));
    }

    private record BranchConfig(int receipts, int revCents) {}

    private static class RowData {
        String branch;
        String date;
        String receiptNo;
        String sku;
        String productName;
        String category;
        int quantity;
        String unitPrice;
        String discount;
        String paymentMethod;
        int lineRevCents;
    }

    private static void generateMonth(Path baseDir, String monthStr, int days, Map<String, BranchConfig> config) throws IOException {
        Path dir = baseDir.resolve(monthStr);
        Files.createDirectories(dir);

        for (String branch : List.of("PNH", "REP", "BTB")) {
            BranchConfig cfg = config.get(branch);
            List<RowData> rows = generateBranchRows(branch, monthStr, days, cfg.receipts, cfg.revCents);

            for (int d = 1; d <= days; d++) {
                String dayStr = String.format("%02d", d);
                String filename = branch + "-" + monthStr + "-" + dayStr + ".csv";
                Path filePath = dir.resolve(filename);

                List<RowData> dayRows = rows.subList((d - 1) * 40, d * 40);
                try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                    writer.write("branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method\n");
                    for (RowData r : dayRows) {
                        writer.write(String.join(",",
                                r.branch, r.date, r.receiptNo, r.sku, r.productName,
                                r.category, String.valueOf(r.quantity), r.unitPrice,
                                r.discount, r.paymentMethod));
                        writer.write("\n");
                    }
                }
            }
        }
    }

    private static List<RowData> generateBranchRows(String branch, String monthStr, int days, int targetReceipts, int targetRevCents) {
        int totalRows = days * 40;
        List<Integer> receipts = new ArrayList<>(totalRows);
        for (int i = 1; i <= targetReceipts; i++) {
            receipts.add(i);
        }
        for (int i = targetReceipts + 1; i <= totalRows; i++) {
            receipts.add(1 + ((i - 1) % targetReceipts));
        }
        receipts.sort(Integer::compareTo);

        List<RowData> rows = new ArrayList<>(totalRows);
        int sumCents = 0;
        for (int i = 0; i < totalRows; i++) {
            int day = (i / 40) + 1;
            String dateStr = String.format("%s-%02d", monthStr, day);
            String rNo = String.format("%s-%06d", branch, receipts.get(i));
            ProductDef p = PRODUCTS.get(i % PRODUCTS.size());
            int qty = 1 + (i % 3);
            int unitPriceCents = (int) Math.round(p.price * 100);
            int lineRevCents = qty * unitPriceCents;
            String payment = PAYMENTS.get(i % PAYMENTS.size());

            RowData r = new RowData();
            r.branch = branch;
            r.date = dateStr;
            r.receiptNo = rNo;
            r.sku = p.sku;
            r.productName = p.name;
            r.category = p.category;
            r.quantity = qty;
            r.unitPrice = String.format(Locale.US, "%.2f", p.price);
            r.discount = "0.00";
            r.paymentMethod = payment;
            r.lineRevCents = lineRevCents;

            rows.add(r);
            sumCents += lineRevCents;
        }

        int diff = targetRevCents - sumCents;
        for (int i = 0; i < totalRows && diff > 0; i++) {
            RowData r = rows.get(i);
            int pPrice = (int) Math.round(Double.parseDouble(r.unitPrice) * 100);
            if (r.quantity < 5 && diff >= pPrice) {
                r.quantity += 1;
                r.lineRevCents += pPrice;
                diff -= pPrice;
            }
        }
        for (int i = 0; i < totalRows && diff < 0; i++) {
            RowData r = rows.get(i);
            int pPrice = (int) Math.round(Double.parseDouble(r.unitPrice) * 100);
            if (r.quantity > 1 && -diff >= pPrice) {
                r.quantity -= 1;
                r.lineRevCents -= pPrice;
                diff += pPrice;
            }
        }

        if (diff != 0) {
            RowData r = rows.get(0);
            int curRev = r.lineRevCents + diff;
            r.quantity = 1;
            r.unitPrice = String.format(Locale.US, "%.2f", curRev / 100.0);
            r.discount = "0.00";
            r.lineRevCents = curRev;
        }

        return rows;
    }
}
