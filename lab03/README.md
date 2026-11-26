# Lab-03: Model-View-ViewModel (MVVM) Design Pattern

**Pull Request:** `Lab-03 – Team` *(link PR once opened on GitHub)*  
**Branch:** `lab-03`  

---

## 1. Files Added / Updated in Lab-03

| Path | Purpose | Author |
| :--- | :--- | :--- |
| `pom.xml` | Updated to `0.3.0-SNAPSHOT`, added `jacoco-maven-plugin` (`0.8.15`), set `app.main` to `ReportViewerApp` | Team |
| `src/main/java/edu/itc/salesreport/ui/Command.java` | Command contract with `executableProperty()` and `execute()` | Team |
| `src/main/java/edu/itc/salesreport/ui/DelegateCommand.java` | Guarded command delegating to a `Runnable` | Team |
| `src/main/java/edu/itc/salesreport/ui/SummarySource.java` | Model port loading one `BranchSummary` per `(YearMonth, branch)` | Team |
| `src/main/java/edu/itc/salesreport/ui/BackgroundJobs.java` | Async execution port with `Work`, `ProgressSink`, `JobListener`, `JobHandle` | Team |
| `src/main/java/edu/itc/salesreport/ui/ReportViewModel.java` | Toolkit-independent MVVM ViewModel (`javafx.base` only) with validation, bindings, commands, async load | Team |
| `src/main/java/edu/itc/salesreport/ui/ReportView.java` | Thin JavaFX view bound to `ReportViewModel` with zero business logic | Team |
| `src/main/java/edu/itc/salesreport/ui/CallerThreadJobs.java` | Synchronous `BackgroundJobs` baseline for frozen-UI comparison | Team |
| `src/main/java/edu/itc/salesreport/ui/FxBackgroundJobs.java` | `javafx.concurrent.Task`-based background job runner | Team |
| `src/main/java/edu/itc/salesreport/ui/FrameGapMonitor.java` | `AnimationTimer` measuring worst FX thread frame gap | Team |
| `src/main/java/edu/itc/salesreport/ui/LoadBenchmarkApp.java` | Benchmark harness comparing `CallerThreadJobs` vs `FxBackgroundJobs` | Team |
| `src/main/java/edu/itc/salesreport/ui/DashboardView.java` | *(Challenge)* Compact secondary view bound to the same `ReportViewModel` | Team |
| `src/main/java/edu/itc/salesreport/ui/DashboardWindow.java` | *(Challenge)* Second JavaFX `Stage` opening `DashboardView` | Team |
| `src/main/java/edu/itc/salesreport/ui/ReportViewerApp.java` | Composition root wiring `SummarySource`, `FxBackgroundJobs`, `ReportView`, and `DashboardWindow` | Team |
| `src/test/java/edu/itc/salesreport/ui/ManualJobs.java` | Synchronous queue-based `BackgroundJobs` test double | Team |
| `src/test/java/edu/itc/salesreport/ui/ReportViewModelTest.java` | 13 headless JUnit 6 tests using fixed `Clock` (`2026-09-27`) | Team |
| `src/test/java/edu/itc/salesreport/ui/ViewModelImportsTest.java` | Architecture guard verifying `ReportViewModel` imports `javafx.base` only | Team |
| `lab03/viewmodel-design.md` | Controller inventory, ViewModel member catalog, class diagram, and state machine | Team |
| `lab03/measurements.md` | 10-run responsiveness benchmark and manual stress-test observation | Team |
| `lab03/mvc-vs-mvvm.md` | Quantitative comparison of Lab-02 MVC vs Lab-03 MVVM | Team |
| `lab03/platform-mapping.md` | *(Challenge)* Android Jetpack & WPF mapping for `ReportViewModel` | Team |
| `lab03/screenshots/viewer.png` & `dashboard.png` | Screenshot deliverables | Team |

---

## 2. Inputs Reused from Lab-02

| Input | Source | Notes |
| :--- | :--- | :--- |
| Domain records (`SaleTransaction`, `BranchSummary`, `ProductTotal`, `PaymentMethod`) | Lab-02 (`src/main/java/edu/itc/salesreport/model/`) | Unchanged |
| CSV ingestion & aggregation (`CsvTransactionParser`, `CsvMonthReader`, `DefaultSalesAggregator`) | Lab-02 (`ingest/`, `engine/`) | Adapted via `SummarySource` lambda in `ReportViewerApp` |
| Sample CSV data (`data/2026-08`, `data/2026-09`) | Lab-02 (`SalesDataGenerator`) | Reused in `data/` |

---

## 3. Change Log

| Date | What Changed | Who | `model/` Package Changed? |
| :--- | :--- | :--- | :--- |
| 2026-11-26 | Implemented MVVM `ReportViewModel`, `ReportView`, `Command`/`DelegateCommand`, `SummarySource`, `BackgroundJobs` (`CallerThreadJobs`, `FxBackgroundJobs`), `LoadBenchmarkApp`, Challenge `DashboardView`/`DashboardWindow`, 13 headless ViewModel tests, `ViewModelImportsTest`, and all `lab03/` docs. | Team | **No** (unchanged since Lab-02) |
