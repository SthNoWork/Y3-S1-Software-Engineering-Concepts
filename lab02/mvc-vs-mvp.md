# MVC vs. MVP Architectural Comparison

## 1. Overview of Evaluated Architectures

This project implements and compares two primary UI architectural patterns for the **Angkor Mart** sales reporting system:
- **Model-View-Controller (MVC):** The View (`ReportView`) observes the Model (`SalesModel`) directly and queries domain objects (`SaleTransaction`, `BranchSummary`) to render tables and charts. User actions on the View are forwarded to the Controller (`ReportController`), which validates inputs and mutates the Model.
- **Model-View-Presenter (Passive View MVP):** The View (`FxReportScreen` implementing `ReportScreen`) is completely passive and holds no reference to `SalesModel` or domain records. The Presenter (`ReportPresenter`) listens to `SalesModel`, converts domain data into formatted display objects (`RowItem`), and pushes primitives/strings to the View via the `ReportScreen` contract.

---

## 2. Detailed Metric Comparison

| Metric / Dimension | Model-View-Controller (MVC) | Passive-View Presenter (MVP) |
| :--- | :--- | :--- |
| **View Implementation SLOC** | ~115 LOC (`ReportView`) | ~95 LOC (`FxReportScreen`) |
| **Presenter / Controller SLOC** | ~50 LOC (`ReportController`) | ~80 LOC (`ReportPresenter` + `ReportController`) |
| **Display Contract SLOC** | 0 LOC (Uses domain models directly) | ~25 LOC (`ReportScreen`, `RowItem`) |
| **Total Presentation SLOC** | ~165 LOC | ~200 LOC |
| **Headless Test Coverage (UI Logic)** | Moderate: Controller tested headlessly, but View binding/formatting requires JavaFX toolkit | **High (100%):** Presenter tested headlessly (`ReportPresenterTest`) without initializing JavaFX |
| **Formatting Responsibilities** | **View:** Formats currency (`$%,.2f`), dates, and labels directly in CellValueFactories | **Presenter:** Formats all strings, currency, and percentages into plain string records (`RowItem`) |
| **Coupling: View to Model** | **Direct:** View imports and reads `SaleTransaction`, `BranchSummary`, and `SalesModel` | **Zero:** View has zero imports of domain model; only depends on `ReportScreen` and `RowItem` |
| **Coupling: Presenter/Controller to View** | **Loose:** Controller depends on abstract `ErrorDisplay` | **Loose:** Presenter depends only on abstract `ReportScreen` interface |
| **UI Toolkit Independence** | Model and Controller are toolkit-free; View is JavaFX-specific | Model, Controller, Presenter, and Contracts are **100% toolkit-free**; only `FxReportScreen` contains JavaFX |

---

## 3. Modification Surface Area: Adding the "Discount" Column

To evaluate maintainability, both implementations were modified to add a new `Discount` column to the transaction table:

### In MVC (`ReportView`):
1. Add `TableColumn<SaleTransaction, String> discCol = new TableColumn<>("Discount");`
2. Define cell value factory extracting `t.discount()`:
   ```java
   discCol.setCellValueFactory(data -> new SimpleStringProperty(
       String.format(Locale.US, "$%,.2f", data.getValue().discount())
   ));
   ```
3. Add column to `table.getColumns().add(discCol)`.
- **Files Modified:** 1 (`ReportView.java`).
- **Required Knowledge:** JavaFX `TableView` APIs, `SaleTransaction` domain record.

### In MVP (`ReportPresenter` + `ReportScreen`):
1. Add `discount` field to `RowItem(..., String discount)`.
2. Update `ReportPresenter.modelChanged()` to format discount:
   ```java
   String.format(Locale.US, "%,.2f", tx.discount())
   ```
3. Update `FxReportScreen` to bind `d.getValue().discount()` to the new table column.
4. Update unit test `FakeScreen` / `ReportPresenterTest`.
- **Files Modified:** 3 (`RowItem.java`, `ReportPresenter.java`, `FxReportScreen.java`).
- **Required Knowledge:** Clean separation between presentation formatting and widget creation.

---

## 4. Architectural Trade-offs & Conclusions

1. **MVC Strengths:**
   - Faster to write with less boilerplate (no intermediary `RowItem` or `ReportScreen` interface).
   - JavaFX data binding can be used directly on domain properties.

2. **MVC Weaknesses:**
   - View logic (such as string formatting, rounding for display, and conditional styling) cannot be verified in fast, headless unit tests.
   - The View is coupled to domain entities; any change to domain entity structure impacts the GUI code.

3. **MVP Strengths:**
   - **Exceptional Testability:** 100% of display decisions, currency formatting, and state-to-text transitions can be tested headlessly using lightweight mocks (`FakeScreen`).
   - **Framework Portability:** The Presenter and contracts are completely UI-agnostic and can drive a Swing, Android, Web, or Console view without alteration.

4. **Verdict:**
   For mission-critical enterprise systems requiring automated regression testing and long-term maintainability, **Passive-View MVP** provides superior architectural isolation. For rapid prototypes or simple desktop CRUD utilities, **MVC** provides adequate separation with lower code ceremony.

