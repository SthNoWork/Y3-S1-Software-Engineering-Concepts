package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.SaleTransaction;
import edu.itc.salesreport.ui.ErrorDisplay;
import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesEvent;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.SalesModelListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.util.Locale;

public class ReportView extends BorderPane implements SalesModelListener, ErrorDisplay {
    private final SalesModel model;

    final TextField monthField = new TextField("2026-09");
    final ComboBox<String> branchBox = new ComboBox<>();
    final TableView<SaleTransaction> table = new TableView<>();
    final BarChart<String, Number> chart;
    final Label status = new Label("Ready");
    final Label error = new Label();

    public ReportView(SalesModel model) {
        this.model = model;

        branchBox.getItems().addAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");

        error.setTextFill(Color.FIREBRICK);

        HBox top = new HBox(10, new Label("Month (yyyy-MM):"), monthField, new Label("Branch:"), branchBox);
        top.setPadding(new Insets(10));
        setTop(top);

        TableColumn<SaleTransaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().date().toString()));

        TableColumn<SaleTransaction, String> rCol = new TableColumn<>("Receipt");
        rCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().receiptNo()));

        TableColumn<SaleTransaction, String> prodCol = new TableColumn<>("Product");
        prodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().productName()));

        TableColumn<SaleTransaction, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().category()));

        TableColumn<SaleTransaction, Number> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().quantity()));

        TableColumn<SaleTransaction, String> revCol = new TableColumn<>("Revenue");
        revCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format(Locale.US, "$%,.2f", data.getValue().lineRevenue())
        ));

        TableColumn<SaleTransaction, String> discCol = new TableColumn<>("Discount");
        discCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format(Locale.US, "$%,.2f", data.getValue().discount())
        ));

        table.getColumns().addAll(dateCol, rCol, prodCol, catCol, qtyCol, revCol, discCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEXIBLE);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue (USD)");

        chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Revenue by category (USD)");
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        SplitPane splitPane = new SplitPane(table, chart);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.6);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        setCenter(splitPane);

        VBox bottom = new VBox(5, status, error);
        bottom.setPadding(new Insets(10));
        setBottom(bottom);
    }

    public void bind(ReportController controller) {
        monthField.setOnAction(e -> controller.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> controller.branchChosen(branchBox.getValue()));
    }

    @Override
    public void modelChanged(SalesEvent event) {
        if (!branchBox.getValue().equalsIgnoreCase(model.branch())) {
            branchBox.setValue(model.branch());
        }

        table.getItems().setAll(model.visibleTransactions());

        BranchSummary summary = model.summary();
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        summary.revenueByCategory().forEach((cat, rev) -> {
            series.getData().add(new XYChart.Data<>(cat, rev.doubleValue()));
        });
        chart.getData().add(series);

        if (event instanceof SalesEvent.MonthLoaded ml) {
            status.setText(String.format(Locale.US,
                    "Loaded %s: %d rows, %d rejected | Branch: %s | Revenue: $%,.2f | Receipts: %d | Avg Basket: $%,.2f",
                    ml.month(), ml.rows(), ml.rejected(), summary.branch(),
                    summary.totalRevenue(), summary.receiptCount(), summary.averageBasket()
            ));
        } else if (event instanceof SalesEvent.BranchSelected bs) {
            status.setText(String.format(Locale.US,
                    "Branch: %s | Revenue: $%,.2f | Receipts: %d | Avg Basket: $%,.2f | Rejected: %d",
                    bs.branch(), summary.totalRevenue(), summary.receiptCount(),
                    summary.averageBasket(), summary.rejectedCount()
            ));
        }
    }

    @Override
    public void showError(String message) {
        error.setText(message);
    }

    @Override
    public void clearError() {
        error.setText("");
    }
}

