# Changelog for 2022

### 2022-07
* :arrow_up: :sparkles: Moved to Rhino 1.7.14. This means many new features, including template strings and better Java interoperability. See full list [on Mozilla's site](https://github.com/mozilla/rhino/releases/tag/Rhino1_7_14_Release). ([#186](https://github.com/bThink-BGU/BPjs/issues/186)).
    * :part_alternation_mark: Refactored internals fo better fit the new Rhino version. We now have a proper context factory, as well as try-with-resources on all context invocations.
* :part_alternation_mark: `BProgramRunnerListener` defautls to ignore errors during BProgram execution, instead of printing the details to `stderr`. This behavior remains in `BProgramRunnerListenerAdapter`, where it makes more sense. Of course, client code can override these methods. ([#191](https://github.com/bThink-BGU/BPjs/issues/191)).
* :arrow_up: Printing problematic b-thread's name during serialization errors ([#169](https://github.com/bThink-BGU/BPjs/issues/169)).
* :sparkles: Custom serialization for the common and non-serializable `java.util.Optional` and JavaScript's `Set()`. ([#189](https://github.com/bThink-BGU/BPjs/issues/189))
    * This is a basis for custom serializations in general, we now have an initial mechanism in place.
* :arrow_up: Improved wrapping of Java objects as they come to JavaScript: Java strings are now treated as native JS strings, so using the `===` operator works as expected. ([#104](https://github.com/bThink-BGU/BPjs/issues/104)).
* :bug: Verification stops on ECMAScript errors, instead of hanging ([#49](https://github.com/bThink-BGU/BPjs/issues/49)).
* :bug: Improved error reporting when trying to sync outside of a b-thread ([#174](https://github.com/bThink-BGU/BPjs/issues/174)).
* :arrow_up: Informative error message when requesting a list of events and one of the events is `null` ([#184](https://github.com/bThink-BGU/BPjs/issues/184)).
* :arrow_up: Logging is turned off by default during verification. Call `BPjs.setLogDuringVerification(true)` to enable them again ([#160](https://github.com/bThink-BGU/BPjs/issues/160)).
* :sparkles: Added local [JACOCO](https://www.jacoco.org) code coverage reports.

### 2022-02 (Including the 1st BP Day Hackathon)
* :part_alternation_mark: Calling `bp.sync` from global scope during runtime does not cause an ugly exception anymore ([#174](https://github.com/bThink-BGU/BPjs/issues/174)). The error is reported to the listeners.
    * Also when making the illegal call from an interrupt handler.
* :arrow_up: Improved JavaScript code error detection during analysis
* :arrow_up: Improvements to handling `null`s in event set arrays ([#178](https://github.com/bThink-BGU/BPjs/issues/178)).
* :arrow_up: Improvements to handling Rhino context in BPjs class ([#176](https://github.com/bThink-BGU/BPjs/issues/176)).
* :arrow_up: Improvements to the `MapProxy` classes, allowing client code pre-process changes.
* :arrow_up: `JsEventSet` now honors the event set name during equality checks.
* :part_alternation_mark: `BEvent::getDataField` data accessors wrap the lower-level `getData()` ([#161](https://github.com/bThink-BGU/BPjs/issues/161)).
* :part_alternation_mark: API improvments on `DfcBProgramVerifier` ([#173](https://github.com/bThink-BGU/BPjs/issues/173)).
* :put_litter_in_its_place: Consolidated tests for `BProgramJsProxy`.

[Earlier Changes](changelog-2021.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles: New feature
* :tada: New feature, but more exciting
* :part_alternation_mark: Refactor (turns out this sign is called "part alternation mark" and not "weird 'M'", so it fits).
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix
