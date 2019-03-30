# BPjs: A JavaScript-based Behavioral Programming Runtime.

This repository contains a javascript-based [BP](http://www.b-prog.org) library.

[![Build Status](https://travis-ci.org/bThink-BGU/BPjs.svg?branch=master)](https://travis-ci.org/bThink-BGU/BPjs)
[![Coverage Status](https://coveralls.io/repos/github/bThink-BGU/BPjs/badge.svg?branch=master)](https://coveralls.io/github/bThink-BGU/BPjs?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bthink-bgu/BPjs/badge.png?style-plastic)](https://repo.maven.apache.org/maven2/com/github/bthink-bgu/BPjs/)
[![Documentation Status](http://readthedocs.org/projects/bpjs/badge/?version=master)](http://bpjs.readthedocs.io/en/master/)
[![JavaDocs](https://img.shields.io/badge/javadocs-browse-green.svg)](http://www.javadoc.io/doc/com.github.bthink-bgu/BPjs/)

#### License
* BPjs is open sourced under the [MIT license](http://www.opensource.org/licenses/mit-license.php). If you use it in a system, please provide
a link to this page somewhere in the documentation/system about section.
* BPjs uses the Mozilla Rhino JavaScript engine. Project page and source code can be found [here](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino).

---

## Getting BPjs
* For Maven projects: Add BPjs as dependency. Note that the version number changes.

````xml
<dependencies>
    ...
    <dependency>
        <groupId>com.github.bthink-bgu</groupId>
        <artifactId>BPjs</artifactId>
        <version>0.10.0</version>
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

## Change Log for the BPjs Library.

### 2019-03-30
* :arrow_up: Improved state hashing
* :arrow_up: More tests for the verifier
* :sparkles: Proper JavaScript error reporting at runtime (e.g. invoking methods of `null`). (#52)
* :sparkles: Proper JavaScript error reporting at for verification. To this end, a new violation, `JSErrorViolation`, was added. (#79)
* :arrow_up: Updated error message when calling `bp.sync` from a non-b-thread context.

### 2019-03-29
* :bug: Fixed a bug in the verifier, where a non-cyclic path ending in a visited node is not inspected.
* :bug: Hash code fixes.
* :arrow_up: Interface cleanups in the DfsBProgramVerifier: some method exposed the internal DFS nodes, and now they don't do that anymore.
* :arrow_up: More tests

### 2019-03-10
* :put_litter_in_its_place: Code cleanup.

### 2019-03-05
* :sparkles: Serious makeover for the composable event suite. Users can no compose event sets using `or`,`and`, and `not` (and even `xor`, `nor` and `nand`) and get semantically correct sets that support verification.
  for example, this works:
```java
EventSet sutA = theEventSet(E_A).or(E_B).and( theEventSet(E_B).or(E_C) );
EventSet sutB = theEventSet(E_C).or(E_B).and( theEventSet(E_B).or(E_A) );
assertEquals( sutA, sutB );
```
* :arrow_up: `ComposableEventSet` refactored to have `equals` and `hashCode` that can hold during verification.
* :arrow_up: More tests.

### 2019-02-10
* :arrow_up: Improved hash functions for `ContinuationProgramState` (affects b-thread sync snapshots as well).

### 2019-02-10
* :bug: Instances of the constant sets class `EventSets` now cannot be duplicated even when they are serialized.
* :bug: Fixed a bug in the verifier, where inspections were invoked twice.
* :arrow_up: Improved hash functions for `SyncStatement`s and `ComposableEventSet`s.
* :arrow_up: `ExecutionTrace`s can now return the last event, in addition to returning the last state.
* :arrow_up: More tests.
* :put_litter_in_its_place: Removed duplicate functionality of "all events except" (had two classes that did this).
* :arrow_up: More robust JavaScript error handling.

### 2019-02-10
* :arrow_up: Updated the Tic-Tac-Toe code.
* :put_litter_in_its_place: Removed dependencies from `pom.xml`
* :arrow_up: Updated versions of dependencies in `pom.xml`.

### 2019-02-18
* :arrow_up: More tests

### 2019-02-18
* :sparkles: Detection of multiple violations/general program space traversal: `DFSBProgramVerifier` can continue its traversal of b-program state space after a violation was detected.
* :arrow_up: Tests clean up after their executor services.
* :arrow_up: More tests

### 2019-02-06
* :arrow_up::arrow_up::sparkles: Verification areas refactored and generalized towards code reuse. Inspectors and traces are now general, and do not assume they were created from the DFS verifier.
* :arrow_up: Improved state hashing algorithm in the hash-based visited state storage.
* :arrow_up: Updated documentation of the verification parts.
* :arrow_up: More tests.
* :arrow_up: Updated maven compiler plugin.
* :bug: Fixed a corner case where b-programs with immediate failed assertions would still attempt running certain machines.

### 2019-02-05
* :arrow_up: :tada: Event selection strategies now accept `BProgramSyncSnapshot`, rather than a set of sync statements and external events.

### 2019-02-04
* :sparkles: Testing infrastructure for execution traces.
* :arrow_up: More terminology cleanups in the api (e.g. "bsync" converted to "sync")

### 2019-02-03
* :arrow_up: `VisitedStateStore` now stores *states*, not DFS nodes. So it's more reusable that way.

### 2019-01-19
* :sparkles: More tests.
* :arrow_up: `BriefPrintDfsVerifierListener` => `PrintDfsVerifierListener`, so that it's consistent with `PrintBProgramRunnerListener`.
* :arrow_up: Incorporated the "BPjs Tips" file to the central documentation.

### 2019-01-18
* :bug: Fixed a that cause equality tests of JS event sets to return false negatives.
* :bug: The `bp` object no longer collected by the state comparators.
* :sparkles: `BEvent` has a bit more informative `toString()` method, that can also look abit into the JavaScript objects (not a full JSON.stringify, since we don't want to deal with arbirtarily large strings for a simple toString op. Not to mention circular references and such).
* :sparkles: `BEvent::hashCode` now hashes data the event might have.
* :sparkles: `ScriptableUtils` got some new utility methods that dig a bit into `ScriptableObject`s. This makes BPjs better at handling events with JS data objects.

### 2019-01-01
* :arrow_up: Hot BThread Cycle now more informative (reporting event and index of return-to).
* :sparkles: Commandline verifier accepts optional `--max-trace-length` parameter, that limits the trace length during verification.
* :arrow_up: Removed the internal `scope` field from `BThreadSyncSnapshot`s. This cuts the size of the serialized form significantly.
* :arrow_up: better hashing algorithm.
* :tada: Happy new year - 2018 changes moved [here](changelof-2018.md).


[Earlier Changes](changelog-2018.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles:New feature
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix
