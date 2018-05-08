=====================
The ``bp`` Object
=====================

In the code snippets we've seen so far, one object kept coming up: ``bp``. This object gives access to the behavioral programming infrastructure that runs the program. It allows registering b-threads, creating events and event sets, and interacting with the external world (e.g. logging).

Another role ``bp`` holds is abstraction layer for values that change, such as random numbers generation and time. This abstraction is needed to allow program verification -- when BP code that uses such values is verified, the verifier needs to iterate over the possible return values. To enable this, the verifier will be able to decide on the values methods such as ``bp.random.nextInt(7)`` will return. For regular program execution, however, calling these methods is just like calling the regular Javascript objects, such as ``Math.random()``.

``bp`` is implemented on the Java side by an object called ``il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy.BProgramJsProxy`` - look at its `JavaDoc`_ for full reference. Below are the noteworthy methods.

.. _JavaDoc: http://javadoc.io/page/com.github.bthink-bgu/BPjs/latest/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/jsproxy/BProgramJsProxy.html


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


``bp.log``
~~~~~~~~~~

Provides access to the logger. See :doc:`logging`.

``bp.random``
~~~~~~~~~~~~~

provides an access to a random number generator, supporting the following methods:

* ``bp.random.nextFloat()``: Generate a random number between 0 and 1.
* ``bp.random.nextInt(bound)``: Generate a random number between 0 (inclusive) and ``bound`` (non-inclusive).
* ``bp.random.nextBoolean()``: Toss a coin, report the caller.

``bp.getTime()``
~~~~~~~~~~~~~~~~

Returns the current time in milliseconds since January 1st, 1970. To get the current date, call ``new Date(bp.getTime())``;
