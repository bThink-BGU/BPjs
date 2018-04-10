# Changelog for 2016

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
* :arrow_up: Upgrade
* :bug: Bug fix