# BPjs: A Javascript-based Behavioral Programming Runtime.

This repository contains a javascript-based [BP](http://www.b-prog.org) library.

[![Build Status](https://travis-ci.org/bThink-BGU/BPjs.svg?branch=develop)](https://travis-ci.org/bThink-BGU/BPjs)
[![Coverage Status](https://coveralls.io/repos/github/bThink-BGU/BPjs/badge.svg?branch=develop)](https://coveralls.io/github/bThink-BGU/BPjs?branch=master)
[![Documentation Status](http://readthedocs.org/projects/bpjs/badge/?version=develop)](http://bpjs.readthedocs.io/en/master/)

#### License
* BPjs is open sourced under the [MIT license](http://www.opensource.org/licenses/mit-license.php). If you use it in a system, please provide
a link to this page somewhere in the documentation/system about section.
* BPjs is uses the Mozilla Rhino Javascript engine. See [here](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino) for project page and source code.

---

## Change log for the BPjs library.

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
