package edu.itc.salesreport.engine;

import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ingest.InvalidRowException;
import edu.itc.salesreport.ingest.TransactionParser;
import edu.itc.salesreport.model.SaleTransaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** File helpers shared by every engine. All methods are safe to call from many threads. */
public final class DataFiles {
    // the parser keeps no state, so one instance can be shared by all threads
    private static final TransactionParser PARSER = new CsvTransactionParser();

    private DataFiles() {}

    /** The CSV files of one month, sorted by name: data/2026-09/BTB-2026-09-01.csv, ... */
    public static List<Path> list(YearMonth month, Path dataDir) throws IOException {
        return list(month, dataDir, "");
    }

    /** The CSV files of one month whose name starts with branch (for example "PNH"). */
    public static List<Path> list(YearMonth month, Path dataDir, String branch)
            throws IOException {
        try (Stream<Path> files = Files.list(dataDir.resolve(month.toString()))) {
            return files.filter(p -> p.getFileName().toString().startsWith(branch))
                    .filter(p -> p.getFileName().toString().endsWith(".csv"))
                    .sorted()
                    .toList();
        }
    }

    /** Reads one file (I/O work). Throws IOException if the header is wrong. */
    public static FileContent read(Path file) throws IOException {
        List<String> all = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (all.isEmpty() || !all.getFirst().equals(TransactionParser.HEADER)) {
            throw new IOException(file.getFileName() + ": unexpected header");
        }
        return new FileContent(file, List.copyOf(all.subList(1, all.size())));
    }

    /** Parses the lines into agg (CPU work). Invalid rows become anomalies. */
    public static void parse(FileContent content, SalesAggregator agg) {
        int lineNo = 2;                                   // line 1 is the header
        for (String line : content.lines()) {
            try {
                agg.add(PARSER.parse(line));
            } catch (InvalidRowException e) {
                agg.addInvalid(content.file().getFileName() + ":" + lineNo
                        + " " + e.getMessage());
            }
            lineNo++;
        }
    }

    /** Reads and parses one file into a NEW aggregator: one task's own result. */
    public static MonthAggregator aggregateFile(Path file) throws IOException {
        MonthAggregator agg = new MonthAggregator();
        parse(read(file), agg);
        return agg;
    }

    /** Every valid row of the month in one list (for fork/join and streams). */
    public static List<SaleTransaction> loadTransactions(YearMonth month, Path dataDir)
            throws IOException {
        List<SaleTransaction> txs = new ArrayList<>();
        for (Path file : list(month, dataDir)) {
            for (String line : read(file).lines()) {
                try {
                    txs.add(PARSER.parse(line));
                } catch (InvalidRowException e) {
                    // skipped: the report engines count these as anomalies
                }
            }
        }
        return txs;
    }
}
