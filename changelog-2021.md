# Changelog for 2021

### 2022-01
* :arrow_up: Improvements to the `BEvent` class, to make it better support client Java code.

### 2021-10
* :sparkles: Added [contribution guidelines](CONTRIBUTING.md) ([#139](https://github.com/bThink-BGU/BPjs/pull/139)).
* :sparkles: Reference to the (finally) published paper describing the updated BPjs model semantics, w.r.t liveness ([#99](https://github.com/bThink-BGU/BPjs/pull/99)).

### 2021-05

* :sparkles: Client code can change the execution services BPjs uses ([#165](https://github.com/bThink-BGU/BPjs/pull/165)).
* :sparkles: `bp.log.XXX` can be redirected to print streams other than `System.out`. ([#159](https://github.com/bThink-BGU/BPjs/pull/159)).
* :arrow_up: Documentation updates.
* :arrow_up: Improved performance of BTSS equality ([#164](https://github.com/bThink-BGU/BPjs/issues/164)).
* :arrow_up: Removed broken JS code in tests ([#153](https://github.com/bThink-BGU/BPjs/issues/153)).


### 2021-02

* :sparkles: :sparkles: :tada: :sparkles: :tada: :sparkles: :rainbow: :sparkles: :sparkles: :sparkles: BPjs now uses Rhino's native continuation equality and hash code ([#116](https://github.com/bThink-BGU/BPjs/issues/116). Also, :tada: :rainbow: :sparkles:.
* :arrow_up: Scope and context creations across BPjs have been consolidated to utility methods in a new class called `BPjs`.
* :arrow_up: Event selection strategies that ignore the synchronization statement data field, issue a warning when b-threads put data there ([#151](https://github.com/bThink-BGU/BPjs/issues/151)).
* :arrow_up: Default priorities in `PrioritizedBSyncEventSelectionStrategy` and `PrioritizedBThreadsEventSelectionStrategy` can be changed ([#115](https://github.com/bThink-BGU/BPjs/issues/115) and [PR#147](https://github.com/bThink-BGU/BPjs/pull/147)).
* :arrow_up: Updated docs.
* :arrow_up: `BProgram` uses an override-able protected method to access its global scope, in case you want to intercept these calls.
* :bug: Fixed an issue that caused `toString` for JavaScript objects to crash at certain cases ([#145](https://github.com/bThink-BGU/BPjs/issues/145)).
* :bug: Storage consolidation conflicts have proper `equals` and `hashCode`.

### 2021-01

* :sparkles: `BProgramSyncSnapshot`s are serialized using a single stream, and with the b-program's original scope as a top-level scope (([#126](https://github.com/bThink-BGU/BPjs/issues/126)).
* :sparkles: BThread snapshots do not retain their entry point function after their first sync point. This results in lower memory footprint, and a more efficient de/serialization. These result in improved analysis performance and efficiency.
* :sparkles: Event sets (and, by inheritance, events) are now composable. So you can write, e.g., `bp.Event("A").or(bp.Event("B")).negate()` to create an event set that contains all events except `A` and `B`.
* :sparkles: Another easy event set composition/creation added: `bp.eventSets` gives access to `EventSets`, with methods such as `bp.eventSets.anyOf(...)` and `bp.eventSets.not(...)`
* :arrow_up: Logging now allows formatting, using Java's MessageFormat.
* :arrow_up: Expressive `toString` on JavaScript sets ([#135](https://github.com/bThink-BGU/BPjs/issues/135)).
* :arrow_up: Error messages when passing non-events to a synchronization statement are more informative ([#131](https://github.com/bThink-BGU/BPjs/issues/131)).
* :arrow_up: Improved logging consistency ([#132](https://github.com/bThink-BGU/BPjs/issues/132)).
* :arrow_up: Error messages when JS event sets do not return a `Boolean` are more informative ([#138](https://github.com/bThink-BGU/BPjs/issues/138)).
* :arrow_up: BThread data documentation updated ([#134](https://github.com/bThink-BGU/BPjs/issues/134)).
* :arrow_up: BProgram storage can be updated by the b-program before b-threads run ([#129](https://github.com/bThink-BGU/BPjs/issues/129)).
* :arrow_up: Changes made to the b-program store after the last sync of a b-thread are now applied ([#130](https://github.com/bThink-BGU/BPjs/issues/130)).
* :arrow_up: More tests (logger, JSProxy, ScriptableUtils, OrderedSet, JsEventSet, some event selection strategies).
* :bug: :tada: JS-semantics are applied for `equals` and `hashCode` for b-thread and b-program data ([#144](https://github.com/bThink-BGU/BPjs/issues/144)).
* :bug: Fixed the note color issue in the documentation ([#137](https://github.com/bThink-BGU/BPjs/issues/137)).
* :bug: Fixed the bench marker.
* :bug: Fixes a crash when a JS event set predicate returns `null` instead of a boolean.
* :put_litter_in_its_place: Significant cleanup of the b-program I/O area.
* :put_litter_in_its_place: Significant cleanup of the event set area. Some methods moved from `ComposableEventSet` to `EventSets`.
* :put_litter_in_its_place: Some documentation updates and corrections.
* :put_litter_in_its_place: Removed extraneous dependencies from `pom.xml` ([#133](https://github.com/bThink-BGU/BPjs/issues/133)).

[Earlier Changes](changelog-2020.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles: New feature
* :tada: New feature, but more exciting
* :part_alternation_mark: Refactor (turns out this sign is called "part alternation mark" and not "weird 'M'", so it fits).
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix
