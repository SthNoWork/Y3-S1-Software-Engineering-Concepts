package edu.itc.salesreport.ui.fx;

import edu.itc.salesreport.ui.ReportController;
import edu.itc.salesreport.ui.SalesModel;
import edu.itc.salesreport.ui.mvp.ReportPresenter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.Clock;

public class ReportMvpApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SalesModel model = new SalesModel();
        FxReportScreen screen = new FxReportScreen();

        Path dataDir = Path.of("data");
        Clock clock = Clock.systemDefaultZone();

        ReportController controller = new ReportController(model, dataDir, clock, screen::showError);
        ReportPresenter presenter = new ReportPresenter(model, screen, controller);
        model.addListener(presenter);
        screen.attach(presenter);

        presenter.monthEntered("2026-09");

        Scene scene = new Scene(screen, 1024, 680);
        primaryStage.setTitle("Angkor Mart Monthly Sales Report (MVP Passive View)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

