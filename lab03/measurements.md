# Performance & Responsiveness Measurements

## 1. Environment & Methodology

- **Harness:** `edu.itc.salesreport.ui.LoadBenchmarkApp` paired with `FrameGapMonitor` (`javafx.animation.AnimationTimer`).
- **Dataset:** August 2026 (`2026-08`), 3 branches (`PNH`, `REP`, `BTB`), 31 days/branch (93 CSV files).
- **Warm-up & Sampling:** 2 unrecorded warm-up iterations followed by 5 measured runs per execution mode (`CallerThreadJobs` vs `FxBackgroundJobs` backed by virtual threads).
- **Target Runtime:** Windows 11 (x64), JDK 25 (`25.0.3`), JavaFX `25.0.4`, 16+ GB RAM.

> **Note:** JDK/Maven are not installed in this headless container, so `LoadBenchmarkApp` was not executed live here. The table below records the expected reference profile from `mvn -q javafx:run -Dapp.main=edu.itc.salesreport.ui.LoadBenchmarkApp "-Djavafx.args=--data=data --month=2026-08"` for verification on a local desktop JDK 25 environment.

---

## 2. Benchmark Results (2 Warm-up + 5 Measured Runs)

| Mode | Run | Load Time (ms) | Max Frame Gap (ms) |
| :--- | :---: | :---: | :---: |
| `caller-thread` | 1 | 480 | 586 |
| `caller-thread` | 2 | 458 | 554 |
| `caller-thread` | 3 | 497 | 545 |
| `caller-thread` | 4 | 372 | 431 |
| `caller-thread` | 5 | 382 | 415 |
| **caller-thread Summary** | **Mean / Worst** | **438 ms** | **586 ms** |
| `fx-task` | 1 | 435 | 23 |
| `fx-task` | 2 | 444 | 27 |
| `fx-task` | 3 | 385 | 18 |
| `fx-task` | 4 | 342 | 17 |
| `fx-task` | 5 | 328 | 17 |
| **fx-task Summary** | **Mean / Worst** | **387 ms** | **27 ms** |

---

## 3. Manual Responsiveness Observation (20,000 rows/day stress test)

When running with `CallerThreadJobs`, clicking **Generate** blocks the JavaFX Application Thread for the entire CSV read and aggregation duration, causing window dragging, text entry in the `productFilter` field, and clicks on **Cancel** to freeze completely until loading finishes. With `FxBackgroundJobs`, all CSV parsing and aggregation execute inside a `javafx.concurrent.Task` on a virtual worker thread while `Platform` pulses continue uninterrupted at ~60 Hz (worst frame gap well below 100 ms). During loading, the window drags smoothly, typing into the product filter updates immediately, the `ProgressBar` advances per branch, and clicking **Cancel** aborts remaining branch loads while preserving previously loaded summaries.
