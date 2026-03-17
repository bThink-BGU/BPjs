# Changelog for 2025

## 2025-12

> [!IMPORTANT]
> This update breaks backward compatibility in some minor ways around logging and custom executor services, as some methods were 
> added and some classes were moved. See below for details. Updates should be pretty straightforward. Contact us if there's any
> issue, we'll be happy to help.

* :arrow_up: Upgraded to Rhino 1.8.1.
* :arrow_up: Upgraded build and test dependencies.
* :bug: Fixed and issued which prevented executor sevices from being re-used. Users who use custom executor services in managed environments now need to use the new methods on `ExecutorService`: `borrow()`/`borrowWithName()` and `returnService()`. Most users will not be affected by this change.[#236](https://github.com/bThink-BGU/BPjs/issues/236)
* :tada: BPLog moved to own package. BPjs now has a central static logger, and no prints should go to `Systen.(out|err)` if you don't want them to. This is still the default behavior, but now messages go through the logging system first, so they can be directed to other logging destinations as well. [#231](https://github.com/bThink-BGU/BPjs/issues/231)
* :arrow_up: `BpLog` is now easier to subclass, as core functionality moved to the interface using `default` methods.
* :sparkles: Added a new logging level: `Error`. Exposed via `bp.log.error("ouch!")`.
* :arrow_up: When logger is in `Fine` level, sotrage modifications are printed to the b-program's logger. [#230](https://github.com/bThink-BGU/BPjs/issues/230)
* :tada: Violations now have location and stack trace. [#224](https://github.com/bThink-BGU/BPjs/issues/224)

## 2025-02
* :arrow_up: Upgraded to Rhino 1.8.0 with updated JavaScript version. See the many updates [here](https://github.com/mozilla/rhino/releases/tag/Rhino1_8_0_Release).
* :arrow_up: ``DfsBProgramVerifier.ProgressListener` now supports default methods for easier implementation.
* :sparkles: Integers are printed as integers, not floats (e.g `5`, not `5.0`).
* :sparkles: Custom serialization is here. Client code can implement serialization logic and register it using `BPjs.registerStubberFactory()`.
    * See examples at `StreamObjectStubTest` and `BuiltInStubberFactory` classes.
    * Followed by [#107](https://github.com/bThink-BGU/BPjs/issues/107) and [#170](https://github.com/bThink-BGU/BPjs/issues/170).
* :sparkles: Event selection strategies always run within a JS context, so any JS logic just works, no manual context wrapping required ([#229](https://github.com/bThink-BGU/BPjs/issues/229)).
* :bug: When performing verification, exceptions during b-program setups are not swalloed anymore ([#228](https://github.com/bThink-BGU/BPjs/issues/228)).
* :bug: Fixed a crash when accessing a b-program's global scope before the program starts to run.
* :arrow_up: Added method `BProgramRunner::getListeners` ([#190](https://github.com/bThink-BGU/BPjs/issues/190)).
* :arrow_up: Setting a non-function interrupt handler (which you should never do anyway) now throws an exception with informative message, rather than being siletly ignored. ([#114](https://github.com/bThink-BGU/BPjs/issues/114)).
* :part_alternation_mark: Stack traces now use standard V8 formatting ([#198](https://github.com/bThink-BGU/BPjs/issues/198)).

## 2025-09
* :sparkles: EventSets now have an `except` method, that allows removal of events from the set. e.g. `allMotionEvents.except(moveDownEvent)`. ([#218](https://github.com/bThink-BGU/BPjs/issues/218))
* :arrow_up: Improved error messages when composing event sets.

## 2025-08
* :part_alternation_mark: `BPjsCodeEvaluationException` is now have one general constructor for all rhino exception([#200](https://github.com/bThink-BGU/BPjs/issues/200))
* :bug: `BProgramRunnerListenerAdapter` is now doing what adapters should do (create empty implementation for abstract classes and interfaces) and does not print on errors anymore. ([#119](https://github.com/bThink-BGU/BPjs/issues/119)).

## 2025-06
* :arrow_up: Printouts and `toString`s of JavaScript compound values (e.g. arrays) adhere to normative JavaScript style. That is, no more `[JS_Array 1,2,3]`, just `[1,2,3]`. Java objects still retain their `J_` prefix.([#213](https://github.com/bThink-BGU/BPjs/issues/213)).

## 2025-05
* :arrow_up: Upgraded dependencies.
* :arrow_up: `MapProxy`'s seed is now `protected`, so client code can use it directly ([#206](https://github.com/bThink-BGU/BPjs/issues/206)).
* :sparkles: Dedicated exceptions for calling `bp.sync` and `bp.thread` outside of a bthread. ([#195](https://github.com/bThink-BGU/BPjs/issues/195), [#203](https://github.com/bThink-BGU/BPjs/issues/203))
* :part_alternation_mark: `BpLog` is now an interface, and it's implementation can be changed by calling `BProgram.setLogger`. ([#208](https://github.com/bThink-BGU/BPjs/issues/208))
* :sparkles: BPjs' version is now available at runtime, using `BPjs.getVersion()`. ([#136](https://github.com/bThink-BGU/BPjs/issues/136))
* :sparkles: BPjs' logger is now is now supporting `bp.log.error`. ([#175](https://github.com/bThink-BGU/BPjs/issues/175))


[Earlier Changes](changelog-2022.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles: New feature
* :tada: New feature, but more exciting
* :part_alternation_mark: Refactor (turns out this sign is called "part alternation mark" and not "weird 'M'", so it fits).
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix
