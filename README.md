# BPjs: A Javascript-based Behavioral Programming Runtime.

This repository contains a javascript-based [BP](http://www.b-prog.org) library.

[![Build Status](https://travis-ci.org/bThink-BGU/BPjs.svg?branch=develop)](https://travis-ci.org/bThink-BGU/BPjs)
[![Coverage Status](https://coveralls.io/repos/github/bThink-BGU/BPjs/badge.svg?branch=develop)](https://coveralls.io/github/bThink-BGU/BPjs?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.bthink-bgu/BPjs/badge.png?style-plastic)](https://repo.maven.apache.org/maven2/com/github/bthink-bgu/BPjs/)
[![Documentation Status](http://readthedocs.org/projects/bpjs/badge/?version=develop)](http://bpjs.readthedocs.io/en/develop/)
[![JavaDocs](https://img.shields.io/badge/javadocs-browse-green.svg)](http://www.javadoc.io/doc/com.github.bthink-bgu/BPjs/)

#### License
* BPjs is open sourced under the [MIT license](http://www.opensource.org/licenses/mit-license.php). If you use it in a system, please provide
a link to this page somewhere in the documentation/system about section.
* BPjs uses the Mozilla Rhino Javascript engine. See [here](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino) for project page and source code.

---

## Getting BPjs
* For Maven projects: Add bpjs as dependency. Note that the version number changes.

````
<dependencies>
    ...
    <dependency>
        <groupId>com.github.bthink-bgu</groupId>
        <artifactId>BPjs</artifactId>
        <version>0.8.4</version>
    </dependency>
    ...
</dependencies>
````

* Clone, fork, or download the [starting project](https://github.com/bThink-BGU/SampleBPjsProject).
* Download the `.jar` files directly from [Maven Central](https://repo.maven.apache.org/maven2/com/github/bthink-bgu/BPjs/).

## Documentation

* Presentations: [Introduction](https://www.slideshare.net/MichaelBarSinai/introducing-bpjs-web)
                 [Deeper dive](https://www.slideshare.net/MichaelBarSinai/deep-dive-into-bpjs)
* [Tutorial and Reference](http://bpjs.readthedocs.io/en/develop/)
* [API Javadocs](http://www.javadoc.io/doc/com.github.bthink-bgu/BPjs/)

## Change log for the BPjs library.

## 2017-08-06
* :sparkles: Added a class to compare continuations (base for comparing snapshots).

## 2017-07-05
* :sparkles: `bsync` now has an extra parameter, allowing b-threads to pass hinting data to custom `EventSelectionStrategy`s.
* :arrows_counterclockwise: Moved event selection strategy to `BProgram`.
* :sparkles: Added a mechanism to log the `BProgramState` at sync points.

## 2017-06-08
* :sparkles: Added documentation for embedding BPjs programs in larger Java apps.

## 2017-05-16
* :sparkles: README includes a more prominent reference to the documentation.

## 2017-05-10
* :sparkles: Added an adapter class for `BProgramListener`.
* :bug: Fixed issues with adding objects to the program's scope.

## 2017-04-08
* :put_litter_in_its_place: Cleaned up the `BProgramRunner`-`BProgram`-`BProgramSyncSnapshot` trio such that listeners don't have to be passed around between them.
* :sparkles: Cloning of `BProgramSyncSnapshot` ready. This is the basis for search.

### 2017-03-22

* :sparkles: New architecture: Running logic moved from `BProgram` to `BProgramRunner` - ongoing.
* :sparkles: `BProgramListener`s notified before BPrograms are started.
* :bug: Fixed a bug where dynamically added b-threads that were added by other dynamically added b-threads would run one cycle too late.
* :bug: Fixed a bug where external events enqueued from top-level JS code where ignored.

### 2017-03-21

* :sparkles: New architecture: Running logic moved from `BProgram` to `BProgramRunner`. This will help implementing search.
* :sparkles: `BProgramListener`s notified when a b-thread runs to completion.
* :sparkles: `bp.getTime()` added.
* :sparkles: Updated tutorial now includes the `bp` object.

### 2017-03-15

* :put_litter_in_its_place: Simplified the `examples` test package.
* :put_litter_in_its_place: `all` and `none` are now only available via `bp`.
* :arrows_counterclockwise: cleaner scope structure..

### 2017-03-14

* :arrows_counterclockwise: Internal method name clean-ups.
* :put_litter_in_its_place: Removed unneeded initializations.
* :bug: Program and bthread scopes are treated as scopes rather than prototypes.
* :sparkles: B-Thread scope games eliminated :tada:. Dynamic b-thread addition is now possible from within loops etc. Tutorial updated.
* :sparkles: More tests.

### 2017-03-02

* :sparkles: `bp.random` added.
* :arrows_counterclockwise: Documentation updates
* :sparkles: Added java accessors for putting and getting variables in the JS program
* :arrows_counterclockwise: `fat.jar` is now `uber.jar`.

### 2017-02-03

* :sparkles: the standard `.jar` file now contains only BPjs, and no dependencies. Fat jar (the jar that includes dependencies) is available via the releases tab.


### 2017-02-02

* :arrows_counterclockwise: `Events` class renamed to `EventSets`. Some cleanup.
* :arrows_counterclockwise: `emptySet` is now `none`.
* :arrows_counterclockwise: `all` and `emptySet` are now available to BPjs code via `bp.all` and `bp.none`. This is to prevent name collisions with client code.
* :bug: Fixed an issue with the logger, where logging levels were ignored.
* :sparkles: Log level can be set by BPjs code, using `bp.log.setLevel(l)`. `l` is one of `Warn`, `Info`, `Fine`.

### 2017-01-16
* :sparkles: Updated documentation to refer to Maven Central
* :bug: `RunFile` re-reads files from file system again.
* :arrows_counterclockwise: More dead code removal.

### 2017-01-14
This release in focused on better BPjs-programmer experience and getting the code into
a maven-central quality grade.
* :bug: Fixing Javadoc references.
* :put_litter_in_its_place: Positional `bsync` removed.
* :sparkles: Better error reporting on event sets defined in JavaScript.
* :sparkles: Better error reports for generic JS errors.
* :sparkles: Added `StringBProgram`: a new class for running BPjs code from a String (rather than a resource or a file).

### 2017-01-13
* :sparkles: Adding a BThread is idempotent. Previously, if a BThread was added twice, it would run twice with unexpected results.
* :sparkles: Basic engine exceptions, plus a friendlier error message when calling `bsync` outside of a BThread.
* :arrows_counterclockwise: More Javadocs and code cleanup (mostly dead code removal).


### 2017-01-12
* :sparkles: License (MIT)
* :sparkles: Preparations for Maven Central
* :arrows_counterclockwise: More Javadocs and code cleanup.


### 2017-01-05
* :sparkles: `RunFile` can now accept multiple BPjs files for input, and runs them as a single BProgram. It also has improved help text.


### 2017-01-03
* :sparkles: Added continuous code coverage with [Coveralls.io](https://coveralls.io/github/bThink-BGU/BPjs?branch=develop) (Thanks guys!).
* :sparkles: Improved test coverage.

### 2017-01-02
* :sparkles: Added continuous testing with [Travis-CI](https://travis-ci.org) (Thanks guys!).
* :tada: Moved from native NetBeans to *maven* project :tada: :tada: :sparkles:
* :bug: Various small issues fixed thanks to static analysis (and NetBeans' Code Inspection tool).
* :arrows_counterclockwise: Moved to canonical package structure (`il.ac.bgu.cs.bp.*`).

### 2017-01-01
* :sparkles: Re-arranged code to minimize amount of `Context.enter`-`Context.exit` pairs (~x5 performance factor!)
* :sparkles: Simplified mechanism for handling event selection in `BProgram`. Replaced a complex Visitor pattern with Java8's `Optional`.
* :arrows_counterclockwise: Improved efficiency for external events handling.
* :sparkles: More tests for `SimpleEventSelectionStrategy`.
* :sparkles: More documentation.
* :sparkles: Better error messages.
* :put_litter_in_its_place: Removed unused code from `BProgram`.
* :put_litter_in_its_place: Removed non-serializable `Optional` from fields.

### 2016-12-31
* :put_litter_in_its_place: Removed `bpjs` from JS scope. Programs must use `bp` now.
* :put_litter_in_its_place: Polished the interface for adding BThreads to a program: 1 method instead of 3.
* :bug: Fixed an issue where external events were re-ordered while checking for daemon mode termination.
* :bug: A BProgram now quits when there are no more BThreads, even if there are enqueued external events.
* :bug: Fixed typos in error messages.
* :arrows_counterclockwise: Reduces method accessibility to minimum (nothing is `public` unless it has to be).
* :sparkles: More documentation.

### 2016-12-16
* :sparkles: A class for running bpjs files.
* :thumbsup: Efficient use of `Context`, so that we don't open and exit it all the time.
* :put_litter_in_its_place: More removal of more unused code. We now have less unused code. Less is more, especially with unused code.

### 2016-12-11
* :arrows_counterclockwise: `breakUpon` is now `interrupt`. This leaves `breakUpon` to be later used as a language construct rather than a `bsync` parameter.

### 2016-12-05
* :put_litter_in_its_place: :sparkles: :elephant: Big refactoring and clean-up towards enabling search. `BThread`s removed from engine - main new concept is that an execution of a BThread is a series of `BThreadSyncSnapshot`, advanced/connected by `BPEngineTask`s. A BProgram is an execution environment for multiple BThreads (plus some state and management code).

### 2016-09-18
* :bug: `breakUpon` handlers are evaluated in the `BThread`'s context, rather than in the `BProgram` one.

### 2016-09-13
#### Client code / Javascript
* :sparkles: Updated the logging mechanism from global, single level to 3-level. Code change required: `bplog("hello") -> bp.log.info("hello)`. Also supports `warn` and `fine`.
* :put_litter_in_its_place: `bpjs` is deprecated (but still works). Please use `bp` now.
* :put_litter_in_its_place: positional bsync deprecated (but still works). Please use the named-argument variant `bsync({request:....})`.
* :put_litter_in_its_place: BThread is not exposed in Javascript via `bt` (that was never used).
* :sparkles: BThreads can now enqueue external events using `bp.enqueueExternalEvent()`.
* :sparkles: BThreads can now specify a function that will be executed if they are removed because an event in their `breakUpon` event set was selected. Use `setBreakUponHandler( function(event){...} )`.

#### Engine/General
* :sparkles: Restructured the engine with JS proxies - javascript code has no direct interaction with the Java engine parts!
* :thumbsup: More unit tests and examples

#### Engine/General
* Restructured the engine with JS proxies - javascript code has no direct interaction with the Java engine parts!
* More unit tests and examples

### 2016-06-11
* :sparkles: BEvents now have an associated `data` object. See example [here](BP-javascript/test/bp/examples/eventswithdata/EventsWithData.js)

* :sparkles: New way of creating `BEvent`s: a static method named `named`:

  ````java
  new BEvent("1stEvent") // old
  BEvent.named("1stEvent") // new and English-like.
  ````

  Future usaged include the ability to reuse event instances, but we're not there yet.

* :sparkles: Added support for Javascript definition of event sets:

  ````javascript
  var sampleSet = bpjs.EventSet( function(e){
    return e.getName().startsWith("1st");
  } );
  ````

### 2016-06-10
* :sparkles: Support for `breakUpon` in `bsync`s:

  ````javascript
  bsync( {request:A, waitFor:B, block:C, breakUpon:D})
  ````
* :sparkles: `SingleResourceBProgram` - a convenience class for the common case of having a BProgram that consists of a
    single file.

### 2016-06-01
* :arrows_counterclockwise: BProgram's `setupProgramScope` gets a scope as parameter. So no need to call `getGlobalScope`, and it's clearer what to do.
* :sparkles: `RWBStatement` now knows which BThread instantiated it
* :sparkles: When a program deadlock, `StreamLoggerListener` would print the `RWBStatement`s of all `BThreads`.


Legend:
* :arrows_counterclockwise: Change
* :sparkles:New feature
* :put_litter_in_its_place: Deprecation
