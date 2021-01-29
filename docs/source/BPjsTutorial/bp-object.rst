=====================
The ``bp`` Object
=====================

In the code snippets we've seen so far, one object kept coming up: ``bp``. This object gives access to the behavioral programming infrastructure that runs the program. It allows registering b-threads, creating events and event sets, and interacting with the external world (e.g. logging).

Another role ``bp`` holds is abstraction layer for values that change, such as random numbers generation and time. This abstraction is needed to allow program verification -- when BP code that uses such values is verified, the verifier needs to iterate over the possible return values. To enable this, the verifier will be able to decide on the values methods such as ``bp.random.nextInt(7)`` will return. For regular program execution, however, calling these methods is just like calling the regular Javascript objects, such as ``Math.random()``.

``bp`` is implemented on the Java side by an object called ``il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy.BProgramJsProxy`` - look at its `JavaDoc`_ for full reference. Below are the noteworthy methods.

.. _JavaDoc: http://javadoc.io/page/com.github.bthink-bgu/BPjs/latest/il/ac/bgu/cs/bp/bpjs/execution/jsproxy/BProgramJsProxy.html


Methods of Note
---------------

``bp.Event(name, [data])``
~~~~~~~~~~~~~~~~~~~~~~~~~~

Creates an event with name and possible data object.

* ``name``: Name of event. String.
* ``data``: Optional object. Additional data for the object.


``bp.EventSet(name, predicate)``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creates an event set: basically a named predicate that accepts events and returns ``true`` for set members.

* ``name``: Name of the event set. String.
* ``predicate``: The set membership predicate. Function that takes one parameter (event) and returns boolean.


``bp.enqueueExternalEvent(event)``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Enqueues an event to the program's external event queue. See :ref:`external_events`.

* ``event``: The event to be enqueued. Event.


``bp.registerBThread([name], bthreadFunction)``
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Registers a new b-thread into the running b-program. The caller may specify a name (useful for making sense of the logs, for example). If no name is specified, an automatic name is generated.

* ``name``: Optional. Name of the b-thread. String.
* ``bthreadFunction``: A no-parameter function that is the body of the b-thread.


.. tip::
    Two (or more) b-threads can have the same name. A unique, meaningful name for each b-thread makes debugging/analysis easier, but it does not effect b-program's semantics. (Having said that, a b-program of 219 b-threads all named "my b-thread" might not be, um, ideal).

``bp.fork()``
~~~~~~~~~~~~~

Splits the calling b-thread to two identical b-threads. On the "parent" b-thread, ``fork`` returns ``0``. On the child b-thread, it returns ``1``. Similar to fork in, e.g., C.

``bp.log``
~~~~~~~~~~

Provides access to the logger. See :doc:`logging`.

``bp.eventSets``
~~~~~~~~~~~~~~~~

Provides access to useful event sets methods and constant:

* ``bp.eventSets.all``: All events (an event set that contains all events).
* ``bp.eventSets.none``: An event set that does not contain any event.
* ``bp.eventSets.anyOf(e1, e2, e3)``: An event set that contains ``e1``, ``e2``, and ``e3``. Similar to ``e1.or(e2).or(e3)``.
* ``bp.eventSets.allOf(s1, s2, s3)``: An event set that is the conjunction of ``s1``, ``s2``, and ``s3`` (so, contains only events that are in ``s1`` and ``s2`` AND ``s3``). Similar to ``e1.and(e2).and(e3)``.
* ``bp.eventSets.not(es42)``: An event set containing all events that are not in ``es42``.

.. note::
    Recall that events are event sets themselves (technically, an event is an event set that contains a single event - itself). So whenever a method expects an event _set_, you can pass a regular event to it.




``bp.random``
~~~~~~~~~~~~~

provides an access to a random number generator, supporting the following methods:

* ``bp.random.nextFloat()``: Generate a random number between 0 and 1.
* ``bp.random.nextInt(bound)``: Generate a random number between 0 (inclusive) and ``bound`` (non-inclusive).
* ``bp.random.nextBoolean()``: Toss a coin, report the caller.

``bp.getTime()``
~~~~~~~~~~~~~~~~

Returns the current time in milliseconds since January 1st, 1970. To get the current date, call ``new Date(bp.getTime())``;
