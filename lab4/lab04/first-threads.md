# Task 1: First threads

The three runs below all produced the same report. The order of the worker
messages changed between runs; this is thread interleaving.

## Run 1

```text
worker-PNH finished file 10 of 30
worker-BTB finished file 10 of 30
worker-REP finished file 10 of 30
worker-PNH finished file 20 of 30
worker-BTB finished file 20 of 30
worker-REP finished file 20 of 30
worker-PNH finished file 30 of 30
worker-BTB finished file 30 of 30
worker-REP finished file 30 of 30
chain revenue 3732255.97 in 398 ms
same report as the sequential engine: true
```

## Run 2

```text
worker-PNH finished file 10 of 30
worker-BTB finished file 10 of 30
worker-REP finished file 10 of 30
worker-REP finished file 20 of 30
worker-BTB finished file 20 of 30
worker-PNH finished file 20 of 30
worker-BTB finished file 30 of 30
worker-REP finished file 30 of 30
worker-PNH finished file 30 of 30
chain revenue 3732255.97 in 438 ms
same report as the sequential engine: true
```

## Run 3

```text
worker-REP finished file 10 of 30
worker-PNH finished file 10 of 30
worker-BTB finished file 10 of 30
worker-BTB finished file 20 of 30
worker-PNH finished file 20 of 30
worker-REP finished file 20 of 30
worker-BTB finished file 30 of 30
worker-PNH finished file 30 of 30
worker-REP finished file 30 of 30
chain revenue 3732255.97 in 404 ms
same report as the sequential engine: true
```

## Experiment A: `run()` instead of `start()`

Replacing `worker.start()` with `worker.run()` runs each worker directly on the
main thread. The output is grouped by branch, the thread name is `main`, and
the branches run one after another instead of concurrently.

## Experiment B: no `join()`

Without the join loop, the main thread can merge a result before its worker has
stored it. This causes a `NullPointerException` while `MonthAggregator.merge`
reads the missing result. The final implementation restores both `start()` and
`join()`.
