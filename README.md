# BPjs: A Javascript-based Behavioral Programming Runtime.

This repository contains a javascript-based [BP](http://www.b-prog.org) library.

[![Build Status](https://travis-ci.org/bThink-BGU/BPjs.svg?branch=master)](https://travis-ci.org/bThink-BGU/BPjs)
[![Coverage Status](https://coveralls.io/repos/github/bThink-BGU/BPjs/badge.svg?branch=master)](https://coveralls.io/github/bThink-BGU/BPjs?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bthink-bgu/BPjs/badge.png?style-plastic)](https://repo.maven.apache.org/maven2/com/github/bthink-bgu/BPjs/)
[![Documentation Status](http://readthedocs.org/projects/bpjs/badge/?version=master)](http://bpjs.readthedocs.io/en/master/)
[![JavaDocs](https://img.shields.io/badge/javadocs-browse-green.svg)](http://www.javadoc.io/doc/com.github.bthink-bgu/BPjs/)

#### License
* BPjs is open sourced under the [MIT license](http://www.opensource.org/licenses/mit-license.php). If you use it in a system, please provide
a link to this page somewhere in the documentation/system about section.
* BPjs uses the Mozilla Rhino Javascript engine. Project page and source code can be found [here](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino).

---

## Getting BPjs
* For Maven projects: Add BPjs as dependency. Note that the version number changes.

````xml
<dependencies>
    ...
    <dependency>
        <groupId>com.github.bthink-bgu</groupId>
        <artifactId>BPjs</artifactId>
        <version>0.9.5</version>
    </dependency>
    ...
</dependencies>
````

* Clone, fork, or download the [starting project](https://github.com/bThink-BGU/SampleBPjsProject).
* Download the `.jar` files directly from [Maven Central](https://repo.maven.apache.org/maven2/com/github/bthink-bgu/BPjs/).
* The project's [Google group](https://groups.google.com/forum/#!forum/bpjs)

## Documentation

* [Devoxx Belgium 2018 talk](https://www.youtube.com/watch?v=PW8VdWA0UcA) introducing Behavioral Programming and BPjs.
* Presentations: [Introduction](https://www.slideshare.net/MichaelBarSinai/introducing-bpjs-web)
                 [Deeper dive](https://www.slideshare.net/MichaelBarSinai/deep-dive-into-bpjs)
* [Tutorial and Reference](http://bpjs.readthedocs.io/en/develop/)
* [API Javadocs](http://www.javadoc.io/doc/com.github.bthink-bgu/BPjs/)

## Change log for the BPjs library.

### 2018-12-25
* :bug: Fixed an off-by-one error in the state count of the DFS verifier. Also, cleaned up some of its design.

### 2018-12-24
* :sparkles: Removed dependencies (closes #69)
* :arrows_counterclockwise: Using a fixed thread pool, rather than a cached one (so preventing cases where too many threads from running at once).
* :bug:      Fixed a HUGE memory leak. Huge.
* :bug:      Fixed a crash in `PrioritizedBThreadsEventSelectionStrategy` when all the events were blocked (#70).
* :sparkles: More tests
* :sparkles: Cleanups
* :put_litter_in_its_place: Setting interrupt handlers, the last BPjs task that was not under `bp` object, is now a `bp` method. So `setInterruptHandler` is now `bp.setInterruptHandler`.

### 2018-12-23
* :put_litter_in_its_place: `BProgram` cannot evaluate resources anymore. This change also removes this ability from running JavaScript b-program. It made no sense anyway, and was not used.
* :sparkles: `SingleResourceBProgram` can now accept multiple resources, and was thus renamed to `ResourceBProgram` (#60). (NOTE: this change breaks program that gave explicit names to
               a `SingleResourceBProgram` in the constructor, as the argument for the name is now interpreted as a resource name. You can use `setName` to set the program name later.
* :sparkles: Automatic event names include the name of the class and an index number.
* :bug: Fixed broken links in the documentation.
* :arrow_up: Improved Javadocs.

### 2018-12-19
* :sparkles: There are two different hot cycle violation inspections: one inspects a hot loop at the b-thread level (default), and the other at the whole b-program level.
* :arrow_up: More tests. 
* :sparkles: Updated the docs to reflect the updates in verification features.

### 2018-12-15
* :sparkles: More verification package updates: now differing between cycle inspections and trace inspections.
* :sparkles: Detection of hot cycles. That is, we can verify liveness requirements. :tada: :tada: :tada:
* :arrow_up: Better hash function for `BThreadSycSnapshot`.

### 2018-12-15
* :sparkles: Verification area gets some well-needed refactoring:
    * Inspections get their own interface (so it's easier to add new ones)
    * Common inspections no live in the `DfsVerificationInspections` utility class.
    * The violations part of `VerificationResult` moved into `Violation` class. This allows much cleaner code for inspecting violation as as an open infrastructure for detecting new types of violations (#61).
* :arrow_up: External Events are now part of the `equals` method of `BProgramSyncSnapshot`. 

### 2018-12-14
* :arrows_counterclockwise: Renamed `BSyncStatement` to `SyncStatement`, as we don't call it "bsync" anymore. Also updated the `toString` method, which still used the pre-historic `RWBStatement` term.
* :sparkles: `SyncStatement`s can now be "hot".
* :arrow_up: Improvements to `BProgram`'s API.

### 2018-11-25
* :sparkles: `fork()` added (#57).

### 2018-11-23
* :bug: Fixed scope issues with b-thread continuations.
* :sparkles: Validated scope unit tests are passing.

### 2018-11-20
* :sparkles: more tests, re: dynamic b-thread addition
* :sparkles: added [Devoxx.be2018 talk](https://www.youtube.com/watch?v=PW8VdWA0UcA) :tada:
* :put_litter_in_its_place: code cleanup
 

### 2018-10-17
* :sparkles: `BProgramRunner` now has a `halt()` method #53.
* :sparkles: Documentation now covering verification (well, the basics).
* :sparkles: A b-progrm's logging level can be set from Java (#50)
* :put_litter_in_its_place: Removed old exception code. More tests.
* :put_litter_in_its_place: `bsync`, deprecated long ago, was removed.

### 2018-10-16
* :arrows_counterclockwise: ambiguous term `daemonMode` changed to `waitForExternalEvents` (#54). 
* :bug: `PrioritizedBSyncEventSelectionStrategy` selected wrong event.

### 2018-10-20
* :sparkles: `BPJsCliRunner` can now directly executed from Maven, using `mvn exec:java`. Pass arguments using `-Dexec.args="args go here"`.
* :sparkles: `BPJsCliRunner` can verify a program. Pass `--verify` as a commandline argument. Use `--full-state-storage` to force the verification to use the full state data when determining whether a state was visited (requires more memory).

### 2018-10-13
* :bug: B-Program setup sequence is consistent for all b-threads, including those in appended code.

### 2018-08-08
* :sparkles: Decorating event selection strategies just go easier with the introduction of `AbstractEventSelectionStrategyDecorator`.
* :sparkles: Added a pausing event selection strategy, to allow pausing and rate-limiting the execution of a BProgram.
* :bug: `PrioritizedBSyncEventSelectionStrategy` deals with its own JavaScript Context properly (fixes #34).
* :sparkles: Added a "what do you want to do" section to the docs, to help newcomers.

### 2108-06-*
* :sparkles: Many more tests and benchmarks.

### 2018-04-26
* :bug: Verifiers can be re-used.
* :sparkles: Support for modern JS features, such as let expressions and generators.
* :sparkles: `bp.sync` prints a warning when a b-thread blocks an event it requests [#10](https://github.com/bThink-BGU/BPjs/issues/10).
* :put_litter_in_its_place: Removed extra logging.
* :arrow_up: More tests.
* :arrow_up: Fixed TicTacToe example: now split into two executing classes (interactive GUI and verification). Same core TTT code is used in both, which proves the point that BPjs can be used for smooth transition from verified model to a full application. :tada:
* :sparkles: Added a [file with useful tips](docs/bpjs-tips.md).
* :sparkles: MOAR UNIT TESTSTSSSTTTSTSS!!!!!!! 

### 2018-03-21
* :sparkles: `bsync` is *deprecated*, in favor of `bp.sync`. The latter can be called from any function, not just the immediate b-thread function.
* :sparkles: `BProgramRunner` now implements Java's `Runnable`.
* :arrow_up: Simplified b-thread scope processing. This means scopes behave closer to what a JavaScript programmer would expect.
* :arrow_up: Javadoc references use latest version (rather than a fixed one).
* :arrows_counterclockwise: Code cleanup.
* :arrows_counterclockwise: Documentation cleanup.
* :arrows_counterclockwise: Old updates from this file moved to "changelog" files.

### 2018-02-25
* :arrow_up: `VisitedStateStore` Adds a `clear()` method.
* :bug: `DfsBProgramVerifier` instances can now be re-used.

### 2018-01-24
* :arrow_up: Improved hashing algorithm on `BThreadStateVisitedNodeStore`.
* :sparkles: Transient caching of thread state in `BThreadSyncSnapshot`s. This improves verification performance, with low memory cost.
* :bug: Removed visited state stores that took incoming state into consideration.
* :arrows_counterclockwise: More mazes in the Mazes example.

### 2018-01-18
* :bug: Fixed a crash where program with failed assertions were intermittently crashing.

### 2018-01-17
* :bug: Verifier now correctly identifies deadlock as a state where there are requested events, but they are all blocked (formerly it just looked for the existence of b-threads).

### 2018-01-12
* :bug: :sparkles: Refactored analysis code, removing the invalid (easy to understand, but invalid) `PathRequirement` based analysis, and using only b-thread now.
                   This design is much cleaner, as it uses less concepts. Also moves us towards "everything is a b-thread" world.
* :sparkles: Added tests to demonstrate the various states a verification can end in.
* :bug: Verifiers and runners terminate their threadpools when they are done using them.

### 2018-01-11
* :sparkles: During forward execution, b-threads can halt execution using `bp.ASSERT(boolean, text)`.
* :arrow_up: Refactored the engine tasks to support raising assertions. Reduced some code duplication in the way.
* :arrow_up: Thread pools executing b-threads are now allocated per-executor/verifier (as opposed to using a single static pool). 


[Earlier Changes](changelog-2017.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles:New feature
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix
