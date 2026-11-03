package edu.itc.salesreport.ui;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import edu.itc.salesreport.ingest.MonthData;
import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;

import java.io.IOException;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class SalesModel {
    private String branch = "ALL";
    private MonthData data = null;
    private final CsvMonthReader reader;
    private final SalesAggregator aggregator;
    private final CopyOnWriteArrayList<SalesModelListener> listeners = new CopyOnWriteArrayList<>();

    public SalesModel() {
        this(new CsvMonthReader(new CsvTransactionParser()), new DefaultSalesAggregator());
    }

    public SalesModel(CsvMonthReader reader, SalesAggregator aggregator) {
        this.reader = reader;
        this.aggregator = aggregator;
    }

    public void addListener(SalesModelListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(SalesModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(SalesEvent event) {
        for (SalesModelListener listener : listeners) {
            listener.modelChanged(event);
        }
    }

    public void selectBranch(String newBranch) {
        String normalized = (newBranch == null || newBranch.isBlank()) ? "ALL" : newBranch.trim().toUpperCase();
        if (!Objects.equals(this.branch, normalized)) {
            this.branch = normalized;
            notifyListeners(new SalesEvent.BranchSelected(this.branch));
        }
    }

    public void load(Path dataDir, YearMonth month) throws IOException {
        MonthData loaded = reader.read(dataDir, month);
        this.data = loaded;
        notifyListeners(new SalesEvent.MonthLoaded(month, loaded.rows().size(), loaded.rejected().size()));
    }

    public String branch() {
        return branch;
    }

    public MonthData data() {
        return data;
    }

    public YearMonth currentMonth() {
        return data != null ? data.month() : null;
    }

    public List<SaleTransaction> visibleTransactions() {
        if (data == null) {
            return List.of();
        }
        if ("ALL".equalsIgnoreCase(branch)) {
            return data.rows();
        }
        return data.rows().stream()
                .filter(t -> t.branch().equalsIgnoreCase(branch))
                .toList();
    }

    public BranchSummary summary() {
        if (data == null) {
            return aggregator.aggregate(branch, List.of(), 0);
        }
        return aggregator.aggregate(branch, data.rows(), data.rejected().size());
    }
}

