package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesModel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.Clock;

public class ReportApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SalesModel model = new SalesModel();
        ReportView view = new ReportView(model);
        model.addListener(view);

        Path dataDir = Path.of("data");
        Clock clock = Clock.systemDefaultZone();
        ReportController controller = new ReportController(model, dataDir, clock, view);
        view.bind(controller);

        controller.monthEntered("2026-09");

        Scene scene = new Scene(view, 1024, 680);
        primaryStage.setTitle("Angkor Mart Monthly Sales Report (MVC)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

