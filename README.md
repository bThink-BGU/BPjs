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

#### Academic Citation

If you use BPjs in an academic work, please consider citing it as:

> Bar-Sinai M., Weiss G. (2021) Verification of Liveness and Safety Properties of Behavioral Programs Using BPjs. In: Margaria T., Steffen B. (eds) Leveraging Applications of Formal Methods, Verification and Validation: Tools and Trends. ISoLA 2020. Lecture Notes in Computer Science, vol 12479. Springer, Cham. https://doi.org/10.1007/978-3-030-83723-5_14

[bibtex](docs/source/Examples_code/bpjs.bib)

---

## Getting BPjs
* For Maven projects: Add BPjs as dependency. Note that the version number changes.

````xml
<dependencies>
    ...
    <dependency>
        <groupId>com.github.bthink-bgu</groupId>
        <artifactId>BPjs</artifactId>
        <version>0.13.0</version>
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

### 2025-02
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
* :part_altertion_mark: Stack traces now use standard V8 formatting ([#198](https://github.com/bThink-BGU/BPjs/issues/198)).

### 2023-09
* :sparkles: EventSets now have an `except` method, that allows removal of events from the set. e.g. `allMotionEvents.except(moveDownEvent)`. ([#218](https://github.com/bThink-BGU/BPjs/issues/218))
* :arrow_up: Improved error messages when composing event sets.

### 2023-08
* :part_alternation_mark: `BPjsCodeEvaluationException` is now have one general constructor for all rhino exception([#200](https://github.com/bThink-BGU/BPjs/issues/200))
* :bug: `BProgramRunnerListenerAdapter` is now doing what adapters should do (create empty implementation for abstract classes and interfaces) and does not print on errors anymore. ([#119](https://github.com/bThink-BGU/BPjs/issues/119)).

### 2023-06
* :arrow_up: Printouts and `toString`s of JavaScript compound values (e.g. arrays) adhere to normative JavaScript style. That is, no more `[JS_Array 1,2,3]`, just `[1,2,3]`. Java objects still retain their `J_` prefix.([#213](https://github.com/bThink-BGU/BPjs/issues/213)).

### 2023-05
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
