Embedding BPjs in an Application
================================

Overview
--------

The BPjs library can be embedded in larger Java programs. The setting is useful in
cases where you want the control logic to be implemented using behavioral programming,
and thus need to translate incoming signals to BP events, and selected BP events to
instructions to, e.g. actuators, databases, or web-services.

The layers of an application running a BPjs ``BProgram`` are described in the figure below.
The BP code (top layer) is the BPjs code, written in Javascript. It can interact with
the BPjs runtime using the :doc:`BPjsTutorial/bp-object`. The host application can make
other Java objects available to the Javascript code, as will be explained later.

The BPjs layer serves as a runtime environment to the BProgram. The host application has
to instantiate a ``BProgram`` object, and pass it to a ``BProgramRunner``. The host application
can listen to events of the running b-program (such as start, end, b-thread added, and - of course - b-event selected).
Additionally, the host application can provide custom event selection strategy, in case
the default one it not good enough.


.. figure:: images/BPjs-stack.png
  :scale: 50%
  :alt:   Running BPjs application stack
  :align: center

  The layers of a running BPjs program. The BP program, written in Javascript,
  it the top layer. It interacts with the BPjs runtime using ``bp``,
  a Javascript object added to its context. The hosting Java application controls
  the BPjs runtime via its API. It can push event to the ``BProgram``'s external
  event queue, and register listeners that are invoked when events are selected.


.. figure:: images/bprogram-running.png
  :alt: Class diagram for running a BProgram
  :align: center

  Class diagram describing the strucutre of an embedded b-program. The client code
  generates a BProgram and a BProgramRunner. The runner object consults its event
  selection strategy when selecting events for the b-program it runs. A list of
  listener objects are informed whenever an event of interest, such as b-thread
  addition or b-event selection, occures.
  *Some methods and properties have been omitted for brevity.*


.. note::
  **Why is ``BProgram`` separated from ``BProgramRunner``?**

  Because a b-program is also
  a model that can be *verified* rather than *ran*. The same ``BProgram``
  object can be passed to a verifier object for verification.



Steps for B-Program Embedding
-----------------------------

Code setup
~~~~~~~~~~

* Add BPjs to your classpath. See :doc:`install-bpjs`.
* Decide which ``BProgram`` subclass you need. `BProgram`_ is an abstract class. Its concrete sub-classes differ on how they obtain their source code. `SingleResourceBProgram`_ reads the code from a resource included with the code (typlically, a .js file bundled in the project's .jars). `StringBProgram`_, on the other hand, takes a Java String as source. Of course, ``BProgram`` can be directly extended as needed.
* Write the BPjs code. The code will interact with the runtime using th ``bp`` object. Additional Java classes can be made available to it by using Rhino's `import directives`_, or by adding Java objects to the program's scope (see below).

At Runtime
~~~~~~~~~~

* Instantiate the proper ``BProgram`` sub-class, and supply it with the source BPjs code.
* If needed, add Java objects to the global b-program scope using `putInGlobalScope`_.
* Instantiate a ``BProgramRunner`` object, and supply it with the ``BProgram`` instance.
* If needed, set a new EventSelectionStrategy
* Add listeners
* In the common case when the program needs to wait for external events (such as GUI interactions), set the ``isDaemon`` property of the ``BProgram`` to ``true``.
* Call ``BProgramRunner::start()``.

The BProgram will start running. Lifecycle and behavioral events will be passed to the listener objects. In case the host application would like to push an external event to the embedded b-program (e.g. because of a network request, or a user click), it should use the ``BProgram``'s `enqueueExternalEvent`_ method.

.. tip::
  BPjs' source code contains many examples of embedded BPjs programs - most of the unit tests that involve a b-program. For a more complete example, refer to the `RunFile`_ class, which implements the command-line tool for running BPjs code.

.. _import directives: https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino/Scripting_Java
.. _BProgram: http://static.javadoc.io/com.github.bthink-bgu/BPjs/0.8.4/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/BProgram.html
.. _SingleResourceBProgram: http://static.javadoc.io/com.github.bthink-bgu/BPjs/0.8.4/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/SingleResourceBProgram.html
.. _StringBProgram: http://static.javadoc.io/com.github.bthink-bgu/BPjs/0.8.4/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/StringBProgram.html
.. _putInGlobalScope: http://static.javadoc.io/com.github.bthink-bgu/BPjs/0.8.4/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/BProgram.html#putInGlobalScope-java.lang.String-java.lang.Object-
.. _enqueueExternalEvent: http://static.javadoc.io/com.github.bthink-bgu/BPjs/0.8.4/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/BProgram.html#enqueueExternalEvent-il.ac.bgu.cs.bp.bpjs.events.BEvent-
.. _RunFile: https://github.com/bThink-BGU/BPjs/blob/develop/src/main/java/il/ac/bgu/cs/bp/bpjs/mains/RunFile.java
