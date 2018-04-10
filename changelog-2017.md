# Changelog for 2017

### 2017-12-28
* :arrow_up: Re-arranged package structure, duplicate and ambiguous packages merged. We now have a clean `model`/`execution`/`analysis` division. 
* :bug: Fixed an equality bug in `OrderedSet`.

### 2017-12-22
* :bug: `BSyncStatement`s now retain information about the b-thread that created them.
* :arrow_up: Now using a single `ExecutorService` for the entire JVM (OK, per class-loader). This makes runtime more efficient, resource-wise.
* :arrows_counterclockwise: Using cached thread execution pool instead of the fork-join one (the former seems to make more sense in a BP context).
* :arrow_up: The Java threads executing the b-threads now have specific names: `bpjs-executor-N` (where `N` is a number starting at 1).
* :sparkles: New method: `bp.getJavaThreadName`: Returns the name of the Java thread executing the b-thread while this method was called. 
* :tada: Some changes in this version were requested by actual users. :tada:
* :sparkles: Documentation updated to mention verification (full-length text to be added post-paper).
* :bug: `BThreadJSProxy.get/setBthread` updated to use capital `T`, like the resp fo the code.
* :arrows_counterclockwise: Test clean-up
* :arrows_counterclockwise: Documentation clean-up

### 2017-11-24
* :sparkles: `BProgram` allows appending and prepending source code programmatically, using `appendSource` and `prependSource`.
              These can be used to add environment simulation without touching the simulated model. Or just to act as includes,
              e.g. for common set-ups.
* :sparkles: Added new class: `PathRequirements`, to hold path requirements that do not require state (e.g. "no deadlock").
* :sparkles: `DfsBProgramVerifier` now has a "debug mode" (set/get via `get/isDebugMode`). On debug mode, it prints
             verbose information to `System.out`.
* :sparkles: Added new class: `BThreadStateVisitedNodeStore`, looks only into the states of the b-threads when deciding whether a 
             search node was already visited or not.
* :bug: `InMemoryEventLoggingListener` cleans its event log when a run begins, so it can be reused for multiple runs.
* :arrows_counterclockwise: Reduced method accessibility in `BProgram`, so subclassers have harder time getting into trouble. 
* :put_trash_in_its_place: `BProgramListener` renamed to `BProgramRunnerListener`, since that is the object it listens to.
* :put_trash_in_its_place: `NoDeadlock` class deleted. Use `PathRequirements.NO_DEADLOCK` instead.
* :sparkles: `PathRequirements.ACCEPT_ALL`, is a new requirement that's always true. Useful for scanning a program state space.

### 2017-11-23
* :arrow_up: `DfsProgramVerifier` uses `FullVisitedNodeStore` by default (preferring correctness over speed in the default case).
* :arrow_up: Updated the Dining Philosopher example to use advanced features. Also added it as a unit test.
* :put_litter_in_its_place: Removed `validation` package.
* :sparkles: `ContinuationProgramState` correctly captures updated variable values. :tada:

### 2017-11-02
* :sparkles: the `DfsBProgramVerifier` is now accepting requirement objects over execution paths, instead of the hard-coded deadlock check.
* :sparkles: new `PathRequirement` class. Requirements are passed to the verifiers for making sure the program conforms to them. Two implementation already present:
    * `NoDeadlock` Breakes when there's a deadlock
    * `EventNotPresent` Breaks when the last event in the ongoing path is a member of a given event set.
* :sparkles: the `DfsBProgramVerifier` is now using listener architecture for reporting progress.
* :sparkles: new event set from bp: `bp.allExcept(es)`.
* :arrow_up: Efficient path stack implementation for `BfsBProgramVerifier` (no copying, reversal, etc.)
* :arrow_up: `Mazes.java` Updates to fully use the new verifier features

### 2017-10-30
* :arrow_up: Re-created program state cloning based on code from @szegedi. Cloning is now faster, more efficient, and can handle storage of events.

### 2017-10-16
* :sparkles: New base class for implementing event selection strategies.
* :sparkles: `OrderedEventSelectionStrategy` - A new event selection strategy that honors the order in which events are requested by a given b-thread.
* :sparkles: `PrioritizedBThreadsEventSelectionStrategy` - A new event selection strategy that can assign priorities to b-threads.
* :sparkles: `PrioritizedBSyncEventSelectionStrategy` - A new event selection strategy that allows b-threads to add priority to their `bsync`s.
* :arrow_up: `LoggingEventSelectionStrategyDecorator` also logs selectable events
* :arrow_up: `BProgram` acts nicer when it has a `null` event selection strategy. 

### 2017-09-10
* :sparkles: Updated to Rhino 1.7.7.2.

### 2017-08-18
* :sparkles: Initial verification added. `DfsBProgramVerifier` scans the states of
  a `BProgram` using DFS, and can return traces where there are no selectable events.

### 2017-08-06
* :sparkles: Added a class to compare continuations (base for comparing snapshots).

### 2017-07-05
* :sparkles: `bsync` now has an extra parameter, allowing b-threads to pass hinting data to custom `EventSelectionStrategy`s.
* :arrows_counterclockwise: Moved event selection strategy to `BProgram`.
* :sparkles: Added a mechanism to log the `BProgramState` at sync points.

### 2017-06-08
* :sparkles: Added documentation for embedding BPjs programs in larger Java apps.

### 2017-05-16
* :sparkles: README includes a more prominent reference to the documentation.

### 2017-05-10
* :sparkles: Added an adapter class for `BProgramListener`.
* :bug: Fixed issues with adding objects to the program's scope.

### 2017-04-08
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

[Earlier Changes](changelog-2016.md)

Legend:
* :arrows_counterclockwise: Change
* :sparkles:New feature
* :put_litter_in_its_place: Deprecation
* :arrow_up: Upgrade
* :bug: Bug fix