# MVC (Lab-02) vs. MVVM (Lab-03) Comparison

## 1. Quantitative Comparison

| Metric | MVC (Lab-02) | MVVM (Lab-03) | How Measured |
| :--- | :--- | :--- | :--- |
| **Lines of presentation code** | 56 (`ReportController`) + 151 (`fx/ReportView`) = **207** | 269 (`ReportViewModel`) + 131 (`ReportView`) + 163 (ports, commands, jobs) = **563** (+46 `DashboardView`) | Line count (`\n`) |
| **Presentation-logic line coverage without toolkit** | `ReportController` tested (`ReportControllerTest`), `fx/ReportView` 0% without toolkit | **>= 85% target** (`ReportViewModelTest` tests 13 scenarios headlessly) | JUnit 6 + JaCoCo 0.8.15 |
| **`javafx.scene` imports in logic class** | 0 in `ReportController`, **11+** in `fx/ReportView` | **0** in `ReportViewModel` (`javafx.base` only, guarded by `ViewModelImportsTest`) | Import inspection |
| **Control types referenced by logic class** | 0 in `ReportController` (via `ErrorDisplay`); `fx/ReportView` binds directly | **0** in `ReportViewModel` | Code inspection |
| **Test run time of presentation logic** | Headless controller only (~0.1s) | ~0.1–0.2s for 13 `ReportViewModelTest` tests without starting JavaFX | JUnit Surefire |
| **UI frozen during heavy load** | **Yes** (synchronous on FX thread) | **No** (`FxBackgroundJobs` offloads to `Task`, frame gap < 100 ms) | `LoadBenchmarkApp` |

---

## 2. Architectural Evaluation & Conclusion

Moving from Lab-02 MVC to Lab-03 MVVM significantly improved testability, concurrency safety, and multi-view reuse. All validation rules, command enablement (`canGenerate`, `cancelCommand`), currency formatting, and async state transitions now live in `ReportViewModel` and are verified by 13 deterministic JUnit tests without initializing the JavaFX toolkit. Binding `DashboardView` to the exact same `ReportViewModel` instance required zero changes to the ViewModel itself, proving clean separation between presentation state and widget layout. On the downside, MVVM introduces more classes and interfaces (`Command`, `SummarySource`, `BackgroundJobs`, `ManualJobs`) and makes binding chains harder to debug when a dependency is omitted from `Bindings.createStringBinding`. For small, synchronous CRUD screens or server-rendered pages (such as `sales-web` with Spring MVC), traditional MVC remains simpler with less ceremony; for rich interactive desktop clients with live validation, background tasks, and synchronized multiple windows, MVVM is clearly worth the extra structure.
