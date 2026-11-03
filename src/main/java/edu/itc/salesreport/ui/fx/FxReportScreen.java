package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.ui.mvp.ReportPresenter;
import edu.itc.salesreport.ui.mvp.ReportScreen;
import edu.itc.salesreport.ui.mvp.RowItem;
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
import java.util.List;
import java.util.Map;

public class FxReportScreen extends BorderPane implements ReportScreen {
    final TextField monthField = new TextField("2026-09");
    final ComboBox<String> branchBox = new ComboBox<>();
    final TableView<RowItem> table = new TableView<>();
    final BarChart<String, Number> chart;
    final Label status = new Label("Ready");
    final Label error = new Label();

    public FxReportScreen() {
        branchBox.getItems().addAll("ALL", "PNH", "REP", "BTB");
        branchBox.setValue("ALL");

        error.setTextFill(Color.FIREBRICK);

        HBox top = new HBox(10, new Label("Month (yyyy-MM):"), monthField, new Label("Branch:"), branchBox);
        top.setPadding(new Insets(10));
        setTop(top);

        TableColumn<RowItem, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date()));

        TableColumn<RowItem, String> rCol = new TableColumn<>("Receipt");
        rCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().receipt()));

        TableColumn<RowItem, String> prodCol = new TableColumn<>("Product");
        prodCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().product()));

        TableColumn<RowItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category()));

        TableColumn<RowItem, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().quantity()));

        TableColumn<RowItem, String> revCol = new TableColumn<>("Revenue");
        revCol.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().revenue()));

        TableColumn<RowItem, String> discCol = new TableColumn<>("Discount");
        discCol.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().discount()));

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

    public void attach(ReportPresenter presenter) {
        monthField.setOnAction(e -> presenter.monthEntered(monthField.getText()));
        branchBox.setOnAction(e -> presenter.branchChosen(branchBox.getValue()));
    }

    @Override
    public void showRows(List<RowItem> rows) {
        table.getItems().setAll(rows);
    }

    @Override
    public void showCategoryRevenue(Map<String, BigDecimal> revenue) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        revenue.forEach((cat, rev) -> series.getData().add(new XYChart.Data<>(cat, rev.doubleValue())));
        chart.getData().add(series);
    }

    @Override
    public void showStatus(String text) {
        status.setText(text);
    }

    @Override
    public void showError(String message) {
        error.setText(message);
    }
}

