package edu.itc.salesreport.ingest;

import edu.itc.salesreport.model.SaleTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CsvMonthReader {
    private static final Pattern FILE_PATTERN = Pattern.compile("^(PNH|REP|BTB)-\\d{4}-\\d{2}-\\d{2}\\.csv$");
    private final TransactionParser parser;

    public CsvMonthReader(TransactionParser parser) {
        this.parser = parser;
    }

    public MonthData read(Path dataDir, YearMonth month) throws IOException {
        Path monthDir = dataDir.resolve(month.toString());
        if (!Files.exists(monthDir) || !Files.isDirectory(monthDir)) {
            throw new NoSuchFileException(monthDir.toString());
        }

        List<Path> matchingFiles;
        try (Stream<Path> stream = Files.list(monthDir)) {
            matchingFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> FILE_PATTERN.matcher(p.getFileName().toString()).matches())
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }

        List<SaleTransaction> transactions = new ArrayList<>();
        List<String> rejected = new ArrayList<>();

        for (Path file : matchingFiles) {
            String fileName = file.getFileName().toString();
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line = reader.readLine(); // skip header
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (line.isBlank()) {
                        continue;
                    }
                    try {
                        SaleTransaction transaction = parser.parse(line);
                        transactions.add(transaction);
                    } catch (InvalidRowException ex) {
                        rejected.add(fileName + ":" + lineNumber + " " + ex.getMessage());
                    }
                }
            }
        }

        return new MonthData(month, transactions, rejected);
    }
}

