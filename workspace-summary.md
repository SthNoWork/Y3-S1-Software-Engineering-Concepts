# Workspace Summary

## Stack & Commands
- **Runtime & Build:** Java 25, Maven 3.9, JavaFX `25.0.4`, JUnit `6.1.3`, JaCoCo `0.8.15`, Spring Boot `3.4.1` (`sales-web/`)
- **Build & Verify:** `mvn -q verify`
- **Run MVVM Desktop (Primary + Dashboard):** `mvn -q javafx:run`
- **Run Benchmark:** `mvn -q javafx:run -Dapp.main=edu.itc.salesreport.ui.LoadBenchmarkApp "-Djavafx.args=--data=data --month=2026-08"`
- **Run MVC / MVP Desktop:** `mvn -q javafx:run -Dapp.main=edu.itc.salesreport.ui.fx.ReportApp` / `...ReportMvpApp`
- **Generate Data:** `java -cp target/classes edu.itc.salesreport.ingest.SalesDataGenerator <month> <rowsPerDay> <seed> <dir>`
- **Run Web Challenge:** `cd sales-web && mvn spring-boot:run`

## Architecture & Boundaries
- `src/main/java/edu/itc/salesreport/model/`: Toolkit-free domain records and enums (unchanged since Lab-02).
- `src/main/java/edu/itc/salesreport/ingest/`: Toolkit-free CSV parser, month reader, and deterministic data generator.
- `src/main/java/edu/itc/salesreport/engine/`: Toolkit-free `BigDecimal` branch/chain aggregation engine.
- `src/main/java/edu/itc/salesreport/ui/`: Lab-03 MVVM (`ReportViewModel` using `javafx.base` only, `Command`, `SummarySource`, `BackgroundJobs`, `ReportView`, `DashboardView`, `ReportViewerApp`, `LoadBenchmarkApp`) + Lab-02 MVC controller/model.
- `src/main/java/edu/itc/salesreport/ui/fx/` & `ui/mvp/` & `ui/console/`: Lab-02 JavaFX MVC/MVP views and CLI listener.
- `src/test/java/edu/itc/salesreport/`: Headless JUnit 6 suites + architecture import guards (`ViewModelImportsTest`, `UiIndependenceTest`).
- `sales-web/`: Spring MVC + Thymeleaf web challenge module.
- `lab02/` & `lab03/`: Lab deliverables, design diagrams, measurements, comparisons, and screenshots.

## File Map
- `pom.xml`: Root Maven config (`0.3.0-SNAPSHOT`), JavaFX 25.0.4, JUnit 6.1.3, JaCoCo 0.8.15, `app.main`.
- `src/main/java/edu/itc/salesreport/model/SaleTransaction.java`: Transaction record + `lineRevenue()`.
- `src/main/java/edu/itc/salesreport/model/BranchSummary.java`: Aggregated branch metrics record.
- `src/main/java/edu/itc/salesreport/model/ProductTotal.java`: Product revenue/quantity record.
- `src/main/java/edu/itc/salesreport/model/PaymentMethod.java`: `CASH`, `CARD`, `KHQR`.
- `src/main/java/edu/itc/salesreport/ingest/CsvTransactionParser.java`: Strict 10-field CSV row validator.
- `src/main/java/edu/itc/salesreport/ingest/CsvMonthReader.java`: Alphabetical CSV reader collecting rejected rows.
- `src/main/java/edu/itc/salesreport/ingest/SalesDataGenerator.java`: Deterministic dataset generator.
- `src/main/java/edu/itc/salesreport/engine/DefaultSalesAggregator.java`: `BigDecimal` metrics & top-10 product aggregator.
- `src/main/java/edu/itc/salesreport/ui/Command.java` & `DelegateCommand.java`: Observable command interface and guarded implementation.
- `src/main/java/edu/itc/salesreport/ui/SummarySource.java` & `BackgroundJobs.java`: Model and async job ports.
- `src/main/java/edu/itc/salesreport/ui/ReportViewModel.java`: MVVM ViewModel (`javafx.base` only) with `Clock` validation, bindings, commands.
- `src/main/java/edu/itc/salesreport/ui/ReportView.java`: Thin MVVM JavaFX view bound to `ReportViewModel`.
- `src/main/java/edu/itc/salesreport/ui/DashboardView.java` & `DashboardWindow.java`: Secondary dashboard view bound to same `ReportViewModel`.
- `src/main/java/edu/itc/salesreport/ui/CallerThreadJobs.java` & `FxBackgroundJobs.java`: Sync baseline and `Task`-based job runner.
- `src/main/java/edu/itc/salesreport/ui/FrameGapMonitor.java` & `LoadBenchmarkApp.java`: FX thread responsiveness benchmark.
- `src/main/java/edu/itc/salesreport/ui/ReportViewerApp.java`: MVVM composition root.
- `src/test/java/edu/itc/salesreport/ui/ManualJobs.java`: Queue-based test double for `BackgroundJobs`.
- `src/test/java/edu/itc/salesreport/ui/ReportViewModelTest.java`: 13 headless ViewModel tests.
- `src/test/java/edu/itc/salesreport/ui/ViewModelImportsTest.java`: Guard banning `javafx.scene/stage/application/concurrent` in `ReportViewModel`.
- `sales-web/src/main/java/edu/itc/salesreport/web/ReportWebController.java`: Spring MVC `/report` endpoint.

## Symbol Index
- `SaleTransaction.lineRevenue() -> BigDecimal` (`SaleTransaction.java:18-21`)
- `CsvTransactionParser.parse(String) -> SaleTransaction` (`CsvTransactionParser.java:16-95`)
- `CsvMonthReader.read(Path, YearMonth) -> MonthData` (`CsvMonthReader.java:24-61`)
- `DefaultSalesAggregator.aggregate(String, List<SaleTransaction>, int) -> BranchSummary` (`DefaultSalesAggregator.java:20-77`)
- `ReportViewModel(SummarySource, BackgroundJobs, Clock)` (`ReportViewModel.java:70-118`)
- `ReportViewModel.generate() -> void` / `loadAll(YearMonth, List<String>, ProgressSink) -> LoadResult` (`ReportViewModel.java:141-214`)
- `ReportView(ReportViewModel)` (`ReportView.java:28-93`)
- `DashboardView(ReportViewModel)` (`DashboardView.java:15-38`)
- `FxBackgroundJobs.submit(Work<T>, JobListener<T>) -> JobHandle` (`FxBackgroundJobs.java:16-25`)
- `ReportWebController.showReport(String, String, Model) -> String` (`ReportWebController.java:22-56`, route `GET /report`)
