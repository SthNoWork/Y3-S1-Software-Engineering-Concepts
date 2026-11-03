# ADR-002: Selection of Presentation Pattern for Sales Reporting System

## Status
Accepted

## Context
The Angkor Mart monthly sales reporting system requires presentation frontends capable of rendering aggregated financial metrics, category breakdowns, and transaction tables across desktop and potentially web environments. 

In Lab-01, domain logic was entangled within procedural scripts. In Lab-02, we evaluated two architectural presentation patterns:
1. **Model-View-Controller (MVC):** View directly observes the Model and renders domain models; Controller mutates the Model based on user events.
2. **Model-View-Presenter (Passive View MVP):** View is an interface-driven display shell; Presenter formats domain state into UI strings and drives the View via the contract.

Key architectural drivers:
- **Headless Testability:** Core business calculations and display formatting must be verifiable in automated CI/CD pipelines without initializing graphical windowing systems (JavaFX / X11 / Wayland).
- **Separation of Concerns:** Zero UI toolkit imports (`javafx.*`, `java.awt.*`, `javax.swing.*`) permitted inside domain, ingestion, engine, and controller layers.
- **Extensibility:** Must support multiple frontends (JavaFX Desktop, CLI, Spring MVC Web) from the same core engine.

## Decision
1. **Adopt Passive-View Model-View-Presenter (MVP)** as the primary architectural standard for interactive rich-client desktop components:
   - Presentation logic and currency formatting are centralized in `ReportPresenter`.
   - Views implement `ReportScreen` and expose zero business state.
   - Display items are decoupled as immutable string records (`RowItem`).
2. **Retain Model-View-Controller (MVC)** as a supported baseline pattern and for Spring Boot Web endpoints (`ReportWebController`), where HTTP request/response semantics naturally map to MVC.
3. **Enforce Toolkit Independence via Automated Arch Guard:** `UiIndependenceTest` runs on every build, scanning `model`, `ingest`, `engine`, `ui`, and `ui/mvp` to fail if any UI framework is imported.

## Consequences

### Positive
- **100% Headless Test Coverage:** The entire presenter and controller lifecycle can be thoroughly tested via `ReportPresenterTest` and `ReportControllerTest` using `FakeScreen` and `FakeErrors`.
- **Clean Architecture Boundaries:** Domain and engine modules are fully reusable across desktop, CLI, and web applications.
- **Isolation of Display Formatting:** Changes to currency formats, locales, or number precision are confined to the Presenter without touching JavaFX widgets.

### Negative
- **Additional Boilerplate:** Requires intermediary display contracts (`ReportScreen`) and formatted records (`RowItem`).
- **Higher File Count:** Creating separate Presenter, View contract, and implementation classes increases the initial development footprint compared to a direct MVC view.

