Alter Event Selection Algorithm
===============================

In standard behavioral programming, the b-thread coordinator, or b-program, is allowed to choose any event that is *requested and not blocked*. Even on modestly sized b-programs, this leaves the event selection algorithm with plenty of wiggle room to make its choice. By default, BPjs will randomly select a requested-and-not-blocked event. Algorithms that make more informed choices are, of course, possible. BPjs makes it easy to develop them, and make them reusable.


Meet ``EventSelectionStrategy``
-------------------------------

Each b-program has an event selection strategy, implementing the `EventSelectionStrategy`_ interface. Given a b-program state at a synchronization point, an ``EventSelectionStrategy`` object can generate a set of selectable events, and then select a single event out of them. Accordingly, the ``EventSelectionStrategy`` interface defines only two methods: ``selectableEvents(...)`` and ``select(...)``.

The event selection strategy is used during both program execution and verification. During verification, the strategy is used to generate the possible selectable events; during execution, the strategy both generates the selectable eventset and selects a single event.

Both of ``EventSelectionStrategy``\'s methods accept the program's state at the synchronization point. This state is composed of all the b-sync statements of participating b-threads, and the external event queue. ``selectableEvents`` returns a plain-old Java ``Set``, that can also possibly be empty. During execution, ``select`` receives the program's state as well as the selectable event set obtained from the call to ``selectableEvents``. It does not return an ``Event``, though -- it returns a richer  ``Optional<EventSelectionResult>`` object.

The `EventSelectionResult`_ object holds a selected event, and a set of indices to events in the external event queue. When receiving an `EventSelectionResult`, the b-program will remove the external events at those indices. This allows an event selection strategy a considerable degree of freedom for dealing with external event sets. For example, it can make the event list act like a set, by passing all the indices of events that are equal to the selected event.


Hinted ``bp.sync``\s
--------------------

Some event selection strategies may depend on internal b-thread state. For example, a b-thread may define a HOT/COLD modality, as defined by Live Sequence Charts. A b-thread in a "HOT" state must advance, whereas a b-thread in a "COLD" state can forever stay in its current position without violating the system's specification.

To this end, the ``bp.sync`` statement in BPjs has an optional second parameter. BPjs makes no assumptions about the type of this parameter - it just stores it in the b-thread's `synchronization statement`_, where is it made available to the event selection strategy through the ``getData()`` method.


Sample Strategy: Priority-Based Selection
-----------------------------------------

Let's look at a sample event selection strategy, based on priority. Under this strategy, the b-threads may add to their ``bp.sync`` statements a "priority" integer. The strategy finds a b-thread with the lowest priority, and selects an event that it requested, and was not blocked. Here's the code, followed by a short discussion.

.. literalinclude:: code/ess.java
  :linenos:
  :language: java


The ``selectableEvents`` method begins by creating a set of all blocked events (lines 6-10). It then iterates over the ``BSyncStatement``\s, maintaining the minimal value it found so far. If a lower value is found, it creates a set of the events it requested and were not blocked (lines 15 and 23). Lastly, it returns the last such set found (line 26).

Since all of the work was done in the ``selectableEvents`` method, the ``select`` method has very little left to do: it selects the first event from the selectable event set passed to it. If that set is empty, it returns the empty ``Optional``.

There are two other interesting methods in this code. ``getValue`` (lines 41-43) extracts the priority integer from the statement. Note that the strategy also deals with a case where no such integer was provided.

``getNotBlocked`` (lines 45-54) takes a ``BSyncStatement`` and a blocked ``EventSet``, and returns the non-blocked subset of events requested by the statement. Note that ``EventSet`` is not ``Set<Event>`` -- ``EventSet`` is a predicate, an interface consisting of a single method: ``contains``. Quite often it will include one or more Javascript functions that have to be evaluated. For this reason, ``getNotBlocked`` has to enter and exit a Javascript execution context (lines 47 and 52, respectively).

The b-program using this event selection strategy is shown below. Note that b-threads "bt-1" and "bt-2" provide a priority integer, while "bt-3" does not.

.. literalinclude:: code/b-prog.js
  :linenos:
  :language: javascript

.. warning:: The above strategy is intended for explanatory purposes, and is probably too simplistic for real-world use. It ignores external events, assumes priorities are unique, and if all the events requested by the b-thread with the lowest priority are blocked, it claims there are no selectable events.

.. tip:: The above strategy and b-program are part of BPjs' `unit tests`_.


.. _EventSelectionStrategy: http://javadoc.io/page/com.github.bthink-bgu/BPjs/latest/il/ac/bgu/cs/bp/bpjs/eventselection/EventSelectionStrategy.html
.. _EventSelectionResult: http://javadoc.io/page/com.github.bthink-bgu/BPjs/latest/il/ac/bgu/cs/bp/bpjs/eventselection/EventSelectionResult.html
.. _synchronization statement: http://javadoc.io/page/com.github.bthink-bgu/BPjs/latest/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/BSyncStatement.html
.. _unit tests: https://github.com/bThink-BGU/BPjs/blob/develop/src/test/java/il/ac/bgu/cs/bp/bpjs/examples/StatementsWithDataTest.java
