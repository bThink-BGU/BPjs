# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What is BPjs

BPjs is a Java framework for executing and verifying **Behavioral Programs (BP)** — concurrent JavaScript programs (b-threads) that synchronize at `bp.sync()` points via events. The core model: each b-thread declares what events it *requests*, *waits for*, *blocks*, and what should *interrupt* it. An event selection strategy resolves these declarations to choose which event fires next.

The framework supports two operating modes: **execution** (run one trace) and **verification** (DFS over all possible traces to find violations).

## Build & Test Commands

```bash
# Build
mvn clean package

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=GeneralTest

# Run a specific test method
mvn test -Dtest=GeneralTest#someMethodName

# Build standalone uber-jar
mvn clean package -P uber-jar

# Generate coverage report (output: target/coverage-reports/index.html)
mvn clean test jacoco:report
```

Test framework: JUnit 4 with Hamcrest. Tests live under `src/test/java` and corresponding JS fixtures under `src/test/resources`.

## Architecture

### Core Loop: Execution

`BProgramRunner` drives the execution cycle:
1. All registered b-threads reach `bp.sync()` — each submits a `SyncStatement`
2. `EventSelectionStrategy` picks one selectable event (requested but not blocked)
3. The chosen event is fired: waiting b-threads are resumed, interrupted b-threads are killed
4. Repeat until no b-threads remain or no event can be selected

### Core Loop: Analysis/Veification

`DfsBProgramVerifier` explores all program traces by cloning `BProgramSyncSnapshot` at each state and performing a DFS over he BProgram's state space.
`VisitedStateStore` implementations (snapshot-level or b-thread-level) detect visited nodes on that state space, and prevent the DFS from getting top an infinite loops when traversing cycles.
`ExecutionTraceInspection` objects define what constitutes a violation (e.g., `HOT_SYSTEM`, `HOT_BTHREADS`, assertion failures).

### Key Classes by Package

| Package | Key Classes | Purpose |
|---|---|---|
| `model` | `BProgram`, `BEvent`, `SyncStatement`, `BProgramSyncSnapshot`, `BThreadSyncSnapshot` | Core domain model |
| `model.eventselection` | `EventSelectionStrategy` + implementations | How events are chosen at each sync point |
| `model.eventsets` | `EventSet`, `JsEventSet`, `ComposableEventSet` | Representations of event collections |
| `execution` | `BProgramRunner` | Runs one BProgram trace |
| `execution.jsproxy` | `BProgramJsProxy` | The `bp` object available in JavaScript; bridges JS ↔ Java |
| `execution.tasks` | `StartBThread`, `ResumeBThread`, `StartFork` | Runnable units for the thread pool |
| `analysis` | `DfsBProgramVerifier`, `ExecutionTrace`, `VisitedStateStore`, `VerificationResult` | Model checking via DFS |
| `analysis.violations` | `Violation`, `FailedAssertionViolation`, `JsErrorViolation` | Violation types |
| `bprogramio` | `BProgramSyncSnapshotCloner`, serialization stubs | State cloning/serialization for verification |
| `bprogramio.log` | `BpLog`, `PrintStreamBpLog`, `BpListLog` | Logging infrastructure |
| `exceptions` | `BPjsRuntimeException`, `BPjsCodeEvaluationException`, etc. | Typed exceptions |
| `internal` | `ScriptableUtils`, `BPjsRhinoContextFactory`, `ExecutorServiceMaker` | Low-level Rhino and threading utilities |
| `mains` | `BPjsCliRunner` | CLI entry point (`--verify`, `--max-trace-length`, `--liveness`) |
| (root) | `BPjs` | Global singleton: Rhino context factory, executor service, stubber registry, global logger |

### JavaScript ↔ Java Bridge

The Rhino JavaScript engine (v1.9.1, supports arrow functions in b-threads) runs user scripts. `BProgramJsProxy` is exposed as `bp` inside JS. It translates `bp.sync(...)`, `bp.log(...)`, `bp.fork(...)`, `bp.registerBThread(...)` into Java operations. Type conversions and scope management go through `ScriptableUtils` and `BPjsRhinoContextFactory`.


### Pluggability

- **Event selection**: implement `EventSelectionStrategy` (or use decorator classes `LoggingEventSelectionStrategyDecorator`, `PausingEventSelectionStrategyDecorator`)
- **Serialization**: register custom `SerializationStubber` via `BPjs.addSerializationStubberFactory()`
- **Verification inspections**: compose `ExecutionTraceInspection` objects

## JavaScript Global Scope

`src/main/resources/globalScopeInit.js` bootstraps the JS environment loaded into every BProgram. Changes here affect all b-thread scripts.

## Java Version & Dependencies

- Java 11 (source and target)
- Rhino 1.9.1 (Mozilla JS engine)
- JUnit 4.13.2 / Hamcrest 2.2 (test)
