# Changelog for 2023

## 2023-09
* :sparkles: EventSets now have an `except` method, that allows removal of events from the set. e.g. `allMotionEvents.except(moveDownEvent)`. ([#218](https://github.com/bThink-BGU/BPjs/issues/218))
* :arrow_up: Improved error messages when composing event sets.

## 2023-08
* :part_alternation_mark: `BPjsCodeEvaluationException` is now have one general constructor for all rhino exception([#200](https://github.com/bThink-BGU/BPjs/issues/200))
* :bug: `BProgramRunnerListenerAdapter` is now doing what adapters should do (create empty implementation for abstract classes and interfaces) and does not print on errors anymore. ([#119](https://github.com/bThink-BGU/BPjs/issues/119)).

## 2023-06
* :arrow_up: Printouts and `toString`s of JavaScript compound values (e.g. arrays) adhere to normative JavaScript style. That is, no more `[JS_Array 1,2,3]`, just `[1,2,3]`. Java objects still retain their `J_` prefix.([#213](https://github.com/bThink-BGU/BPjs/issues/213)).

## 2023-05
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
