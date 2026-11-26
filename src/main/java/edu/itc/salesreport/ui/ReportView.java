package edu.itc.salesreport.ui;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.ProductTotal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** MVVM View: build controls and bind them to ReportViewModel. No logic here. */
public final class ReportView {
    private final BorderPane root = new BorderPane();

    public ReportView(ReportViewModel vm) {
        ComboBox<YearMonth> month = new ComboBox<>(vm.monthChoices());
        month.setEditable(true);
        month.setConverter(new YearMonthConverter());
        month.valueProperty().bindBidirectional(vm.monthProperty());
        month.styleProperty().bind(Bindings
                .when(vm.monthErrorProperty().isEmpty())
                .then("")
                .otherwise("-fx-border-color: crimson;"));

        HBox filters = new HBox(8, new Label("Month"), month);
        for (String b : ReportViewModel.BRANCHES) {
            CheckBox box = new CheckBox(b);
            box.selectedProperty().bindBidirectional(vm.branchSelectedProperty(b));
            filters.getChildren().add(box);
        }

        Button generate = new Button("Generate");
        generate.setOnAction(e -> vm.generateCommand().execute());
        generate.disableProperty().bind(Bindings.not(vm.generateCommand().executableProperty()));

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> vm.cancelCommand().execute());
        cancel.disableProperty().bind(Bindings.not(vm.cancelCommand().executableProperty()));

        filters.getChildren().addAll(generate, cancel);

        ProgressBar bar = new ProgressBar();
        bar.progressProperty().bind(vm.progressProperty());
        bar.visibleProperty().bind(vm.loadingProperty());

        Label validation = new Label();
        validation.setStyle("-fx-text-fill: crimson;");
        validation.textProperty().bind(vm.validationMessageProperty());

        Label status = new Label();
        status.textProperty().bind(vm.statusProperty());

        Label error = new Label();
        error.setStyle("-fx-text-fill: firebrick;");
        error.textProperty().bind(vm.loadErrorProperty());

        Label total = new Label();
        total.setStyle("-fx-font-weight: bold;");
        total.textProperty().bind(vm.totalRevenueTextProperty());

        TableView<BranchSummary> table = new TableView<>(vm.summaries());
        table.getColumns().add(column("Branch", BranchSummary::branch));
        table.getColumns().add(column("Revenue", s -> s.totalRevenue().toPlainString()));
        table.getColumns().add(column("Discounts", s -> s.totalDiscount().toPlainString()));
        table.getColumns().add(column("Receipts", s -> String.valueOf(s.receiptCount())));
        table.getColumns().add(column("Avg basket", s -> s.averageBasket().toPlainString()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEXIBLE);

        TextField search = new TextField();
        search.setPromptText("Filter products");
        search.textProperty().bindBidirectional(vm.productFilterProperty());

        ListView<ProductTotal> productList = new ListView<>(vm.visibleProducts());

        VBox top = new VBox(6, filters, validation, new HBox(8, bar, status, error));
        top.setPadding(new Insets(10));
        root.setTop(top);
        root.setCenter(table);

        VBox right = new VBox(6, new Label("Top products"), search, productList);
        right.setPadding(new Insets(10));
        root.setRight(right);

        HBox bottom = new HBox(8, new Label("Chain revenue:"), total);
        bottom.setPadding(new Insets(10));
        root.setBottom(bottom);
    }

    private static TableColumn<BranchSummary, String> column(String title, Function<BranchSummary, String> value) {
        TableColumn<BranchSummary, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cell -> new SimpleStringProperty(value.apply(cell.getValue())));
        return c;
    }

    public Parent root() {
        return root;
    }

    /** Text "2026-09" to YearMonth and back; null for unparsable input. */
    static final class YearMonthConverter extends StringConverter<YearMonth> {
        @Override
        public String toString(YearMonth m) {
            return m == null ? "" : m.toString();
        }

        @Override
        public YearMonth fromString(String s) {
            try {
                return s == null || s.isBlank() ? null : YearMonth.parse(s.strip());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
