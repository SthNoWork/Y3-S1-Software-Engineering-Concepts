# Lab-02: Model-View-Controller (MVC) & Model-View-Presenter (MVP)

**Software Engineering Concepts · Chapter 02**  
* **System:** Angkor Mart Monthly Sales Reporting System  
* **Stack:** Java 25, JavaFX 25.0.4, JUnit 6.1.3, Maven 3.9, Spring Boot 4.1.1 (Web Challenge)  

---

## 1. System Overview

Angkor Mart operates retail branches in Phnom Penh (`PNH`), Siem Reap (`REP`), and Battambang (`BTB`). This project implements a modular, toolkit-free monthly sales reporting system supporting multiple presentation frontends:
1. **Console Terminal View:** Headless CLI report generator.
2. **JavaFX MVC Desktop View:** Interactive table and category bar chart using the Model-View-Controller pattern.
3. **JavaFX MVP Passive View:** Fully decoupled presentation with headless-testable presenter.
4. **Spring MVC Web Dashboard:** Browser-based reporting interface powered by Spring Boot and Thymeleaf.

---

## 2. File Manifest

```
.
├── pom.xml                                    # Root Maven configuration with JavaFX & JUnit BOM
├── .gitignore                                 # Ignore target/, data/, IDE artifacts
├── src/main/java/edu/itc/salesreport/
│   ├── model/
│   │   ├── PaymentMethod.java                 # Enum: CASH, CARD, KHQR
│   │   ├── SaleTransaction.java               # Canonical record with BigDecimal line revenue
│   │   ├── ProductTotal.java                  # Product aggregation record
│   │   └── BranchSummary.java                 # Aggregated metrics container
│   ├── ingest/
│   │   ├── InvalidRowException.java           # Parsing/validation exception
│   │   ├── TransactionParser.java             # Parsing strategy contract
│   │   ├── CsvTransactionParser.java          # Strict business-rule CSV row parser
│   │   ├── MonthData.java                     # Immutable container of valid rows & rejected errors
│   │   ├── CsvMonthReader.java                # Alphabetical directory loader & validator
│   │   └── SalesDataGenerator.java            # Deterministic dataset generator
│   ├── engine/
│   │   ├── SalesAggregator.java               # Aggregation service interface
│   │   └── DefaultSalesAggregator.java        # Revenue, basket size, category & top-10 engine
│   ├── ui/
│   │   ├── SalesEvent.java                    # Sealed domain event hierarchy
│   │   ├── SalesModelListener.java            # Observer contract
│   │   ├── SalesModel.java                    # Toolkit-free observable state model
│   │   ├── ErrorDisplay.java                  # Error reporting contract
│   │   ├── Validation.java                    # Sealed functional result type (Valid/Invalid)
│   │   ├── InputRules.java                    # Pure validation functions
│   │   ├── ReportCommand.java                 # Sealed command hierarchy (LoadMonth, SelectBranch)
│   │   ├── ReportController.java              # Central input validator & model orchestrator
│   │   ├── console/
│   │   │   └── ConsoleReportView.java         # Headless console listener
│   │   ├── fx/
│   │   │   ├── ReportView.java                # JavaFX MVC view (table, chart, filters)
│   │   │   ├── ReportApp.java                 # JavaFX MVC application entry point
│   │   │   ├── FxReportScreen.java            # JavaFX implementation of MVP passive view
│   │   │   ├── ReportMvpApp.java              # JavaFX MVP application entry point
│   │   │   └── legacy/
│   │   │       └── FatReportController.java   # Refactored legacy controller delegating to ReportController
│   │   └── mvp/
│   │       ├── RowItem.java                   # Toolkit-free formatted string row
│   │       ├── ReportScreen.java              # Passive view contract
│   │       └── ReportPresenter.java           # Presenter orchestrating model and screen
├── src/test/java/edu/itc/salesreport/
│   ├── TestData.java                          # Fixture dataset generator (52.00 chain revenue)
│   ├── SalesModelTest.java                    # Observable model tests
│   ├── CsvTransactionParserTest.java          # Strict parser unit tests (11 corrupt variants)
│   ├── InputRulesTest.java                    # Pure validation tests with fixed clock
│   ├── ReportControllerTest.java              # Headless controller tests with FakeErrors
│   ├── ReportPresenterTest.java               # Headless MVP presenter tests with FakeScreen
│   ├── UiIndependenceTest.java                # Architectural boundary guard
│   └── DependencyRulesTest.java               # Test suite bridge for architecture verification
├── lab02/
│   ├── README.md                              # This manifest & overview
│   ├── screenshots/
│   │   ├── mvc-viewer.png                     # Task 2 deliverable
│   │   └── validation.png                     # Task 3 deliverable
│   ├── refactoring.md                         # Task 4 legacy controller review
│   ├── mvc-vs-mvp.md                          # Task 5 MVC vs MVP comparative analysis
│   └── adr-002-presentation-pattern.md        # Task 5 Architecture Decision Record
└── sales-web/                                 # Optional Challenge Spring MVC web application
    ├── pom.xml
    └── src/
        ├── main/java/edu/itc/salesreport/web/
        │   ├── SalesWebApplication.java
        │   └── ReportWebController.java
        ├── main/resources/templates/
        │   └── report.html
        └── test/java/edu/itc/salesreport/web/
            └── ReportWebControllerTest.java
```

---

## 3. Reused Inputs & Domain Specifications

- **Branch Codes:** `PNH` (Phnom Penh), `REP` (Siem Reap), `BTB` (Battambang), `ALL` (Aggregate).
- **Payment Methods:** `CASH`, `CARD`, `KHQR`.
- **Product Categories:** `Grocery`, `Beverages`, `Household`, `Personal Care`, `Electronics`, `Stationery`.
- **Data File Format:** `data/<yyyy-MM>/<BRANCH>-<yyyy-MM-dd>.csv` with header:
  `branch,date,receipt_no,sku,product_name,category,quantity,unit_price,discount,payment_method`
- **Arithmetic Precision:** All financial calculations use `BigDecimal` with scale 2 and `RoundingMode.HALF_UP`.
- **Row Validation:** Corrupted rows are isolated and counted in `rejectedCount`; never included in totals.

---

## 4. Execution & Verification

### Generate Deterministic Data
```bash
mvn compile
java -cp target/classes edu.itc.salesreport.ingest.SalesDataGenerator data
```
- `2026-08`: 93 files, 3,720 rows.
- `2026-09`: 90 files, 3,600 rows.

### Run Headless Verification Tests
```bash
mvn clean test
```
All unit tests execute without opening a display or requiring a graphical environment.

### Run Desktop JavaFX MVC Viewer
```bash
mvn javafx:run
```

### Run Desktop JavaFX MVP Passive-View Viewer
```bash
mvn javafx:run -Djavafx.mainClass=edu.itc.salesreport.ui.fx.ReportMvpApp
```

### Run Spring MVC Web Dashboard
```bash
cd sales-web
mvn spring-boot:run
```
Visit: `http://localhost:8080/report?month=2026-09&branch=ALL`

---

## 5. Version Changelog

- **v0.1.0-SNAPSHOT:** Initial domain modeling and starter CSV ingest.
- **v0.2.0-SNAPSHOT:** 
  - Implementation of toolkit-free `SalesModel` and `DefaultSalesAggregator`.
  - Addition of JavaFX `ReportView` and `ReportApp`.
  - Implementation of `ReportController` with pure `InputRules` validation and `ReportCommand`.
  - Refactoring of `FatReportController` with architectural flaw documentation.
  - Implementation of Model-View-Presenter (MVP) passive view with `RowItem`, `ReportScreen`, and `ReportPresenter`.
  - Addition of `Discount` column to both MVC and MVP presentations.
  - Creation of `sales-web` challenge module utilizing Spring MVC and Thymeleaf.

