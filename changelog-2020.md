# Changelog for 2020

### 2020-12-06
* :sparkles: BProgram data storage ready (closes[#123](https://github.com/bThink-BGU/BPjs/issues/123)).

### 2020-12-04
* :bug: Forking now handles b-thread data properly.

### 2020-11-15
* :sparkles: A B-Program can have multiple b-threads with the same name (closes[#112](https://github.com/bThink-BGU/BPjs/issues/112)).

### 2020-11-09
* :arrow_up: Trace's `getLastEvent` now return an `Optional<Event>` rather than an `Event`. This is because some traces may not have a last event (part of the fix to [#122](https://github.com/bThink-BGU/BPjs/issues/122)). 

### 2020-10-20
* :arrows_counterclockwise: `BEvent::toString` is more JS-like.

### 2020-10-10
* :arrows_counterclockwise: Internal refactorings.
* :bug: JavaScript errors (regarding syntax and references) are reported, and halt both execution and analysis [#94](https://github.com/bThink-BGU/BPjs/issues/94).


### 2020-09-23
* :tada: B-Threads have a data object, available to b-thread code via `bp.thread.data`. This object can be set, updated, and 
    replaced during execution. It can also be pre-set on b-thread creation [#106](https://github.com/bThink-BGU/BPjs/issues/106).
* :sparkles: DFS scan that uses forgetful state storage is not resilient to cyclic state graphs [#95](https://github.com/bThink-BGU/BPjs/issues/95).

### 2020-09-22
* :arrow_up: Added a b-thread name for the self-blocking warning [#103](https://github.com/bThink-BGU/BPjs/issues/103).
* :arrow_up: Logging supports compound objects [#102](https://github.com/bThink-BGU/BPjs/issues/102).
* :sparkles: B-thread name is available to JavaScript code.
* :bug: All JS code is evaluated as ES6 (formerly, evaluations where optimized ES1.8).
* :bug: Improved self-blocking warning logic.

### 2020-08-10
* :put_litter_in_its_place: Unification of some simple event sets, e.g. `[es1]` is identical to `es1`, `[]` to `bp.none`, etc.
* :put_litter_in_its_place: Removed some printouts.

### 2020-08-09
* :arrow_up: Updated to use Rhino 1.7.13
* :arrow_up: Updated to Java 11
* :sparkles: A new type of violation: hot run, which looks at hot runs of a subset of a b-program b-threads. 
    Previously, BPjs supported all b-threads ("hot system run") or just a single one ("hot b-thread run").
* :part_alternation_mark: Liveness verification parts now aligned semantically with current theory.
* :put_litter_in_its_place: Code and documentation clean-ups.

### 2020-04-22
* :arrow_up: Updated to use Rhino 1.7.12
* :arrow_up: :tada: Can use ES6 syntax, such as `let` and `const`.
    * No arrow functions yet, there are some issues with continuations and arrow functions.
* :arrow_up: Synchronization statements are created more efficiently.
* :bug: `ComposableEventSet` checks arguments on creation, so that it can't be instantiated with `null` events (this later causes NPEs).
* :bug: `PrioritizedBThread` ESS fixed ([#70](https://github.com/bThink-BGU/BPjs/issues/70).
* :put_litter_in_its_place: Various small code clean-ups.
* :put_litter_in_its_place: Removed extraneous dependencies from `pom.xml`

[Earlier Changes](changelog-2019.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles: New feature
* :tada: New feature, but more exciting
* :part_alternation_mark: Refactor (turns out this sign is called "part alternation mark" and not "weird 'M'", so it fits).
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix
