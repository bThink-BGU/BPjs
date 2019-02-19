===============
Verification
===============

BPjs programs can be verified for conforming to a set of formal requirements. It is possible to verify both `safety`_ and `liveness`_ properties. As a safety example, BPjs can be used to verify that a b-program will never end up in a deadlock, or that a certain event could never be selected. As for liveness, BPjs can be used to verify that a certain event will happen eventually.

During the verification process, BPjs traverses a b-program's state-space using a depth-first algorithm (the depth can have an upper bound). Whenever it encounters a new state, it checks whether the new state, or the trace leading to it, violates a specification. Whenever it discovers a new cycle, it checks the cycle for violations.

Verification is done directly on the JavaScript code. There is no model transformation involved.

Verifying Safety Properties
---------------------------

BPjs models are verified for most safety properties using assertions. During execution, b-threads may call ``bp.ASSERT(cond, message)``. If ``cond`` evaluates to ``false``, the b-program is considered in violation of its requirements. This mechanism allows intuitive verification of requirements such as *while flying, the doors can't be opened*:

.. code-block:: JavaScript 

  bp.registerBThread(function(){
    while ( true ) {
      bp.sync({waitFor:FLYING});
      bp.sync({waitFor:DOOR_OPENED, interrupt:LANDED});
      bp.ASSERT(false, "Can't open the doors when flying");
    }
  });

Assertions can be used with any boolean expression. They can be used at runtime too, where a false assertion causes a b-program to terminate.

An interesting corner case for safety properties is deadlock detection. When in deadlock, none of the b-threads can advance. In particular, when a b-program is deadlocked, none of its b-threads can invoke a false assertion. Thus, deadlocks are detected using a specific inspector, and not using a b-thread.

.. tip:: ``message`` is an optional parameter explaining, in human-readable terms, what went wrong.

Verifying Liveness Properties
-----------------------------

Liveness properties require that "something good will happen eventually". In order to specify this, BPjs borrows the concept of "hot" locations from its ancestor, `Live Sequence Charts`_ (LSC). A b-thread can declare a ``sync`` as "hot", which means that it must eventually advance beyond it. For example, a requirement such as *any request must receive a response*, can be verified using the following b-thread::

  bp.registerBThread(function(){
    while ( true ) {
      var req = bp.sync({waitFor:ANY_REQUEST});
      bp.hot(true).sync({waitFor:responseFor(req)});
    }
  });

The first ``bp.sync`` is a non-hot ("cold") sync, which means the writer of the b-thread is fine with that b-thread being stuck there forever (for example, if requests stop arriving). The second sync, on the other hand, is marked as hot (by calling ``bp.hot(true)`` before calling ``sync``). This means that the writer of the b-thread does not allow the b-thread to be stuck there forever; eventually, a response to the received request must be sent. During verification, if the verifier finds a trace where this b-thread does not advance beyond the second sync (either because of a loop or because the program terminates), it will report that trace as a violation.

Liveness violations can span more than a single hot sync. In fact, when a b-thread can get into an infinite loop where all its sync points are hot, that loop is considered to be violating a liveness property.

DFS over B-Program States
=========================

Program verification is done by scanning all possible states a program can be in, and testing whether any of these states does violates some requirement. This is of course quite complex, as often the amount of states a program can be in is very large, or even unbounded. Some techniques for alleviating this problems exist; however, they are beyond the scope of this document.

For b-programs, we can describe the state space as a graph, where synchronization points are the vertices and the edges are events that are requested and not blocked at their source state. See the image below for a simple example.

.. figure:: ../images/state-space.png
  :alt: A part of a b-program's state-space.
  :align: center
  :width: 300px

  A state space of a sample b-program with 3 b-threads. Synchronization points are vertices, and events that are requested and not blocked serve as edges that connect between them.

In order to traverse the state space, we use a ``DfsBProgramVerifier``, like so:

.. literalinclude:: ../Examples_code/DfsVerifierSample.java
  :linenos:
  :language: java

The verifier performs a DFS (depth-first search) on the state space. This is of course quite a naive algorithm and we can do better ones (we certainly plan to). Still. DFS works pretty well for small state spaces. In case the depth of a run is unbounded, the length of the scanned path can be limited.

A more in-depth discussion of verification in BPjs, including some techniques, can be found in the `BPjs paper`_ at arXiv.

.. note:: It is possible to select which inspections are preformed during verification. A ``DfsBProgramVerifier`` holds a set of inspectors, which can be added to by calling its ``addInspector`` methods. If no inspectors are added, a default set will be used.

From the Command Line
~~~~~~~~~~~~~~~~~~~~~

To verify a b-program from the commandline, simply add the ``--verify`` switch. Use ``--full-state-storage`` to force the verification to use the full state data when determining whether a state was visited (requires more memory). To concentrate on liveness violations, add the ``--liveness`` switch. To limit the (possibly infinite) depth of the scan, use ``--mac-trace-length``:

.. code-block:: bash

  $ java -jar bpjs.jar --verify hello-possibly-broken-world.js
  $ java -jar bpjs.jar --verify --full-state-storage hello-possibly-broken-world.js
  $ java -jar bpjs.jar --verify --liveness --full-state-storage hello-possibly-broken-world.js


Assertions During Runtime
=========================

During normal execution (i.e. by a ``BProgramRunner``), failed assertion cause program execution to terminate. The execution BProgramRunner's listeners are notified of this failure. This approach is sometimes called "runtime verification", and is useful to implement "emergency shutdown" mechanisms.


.. note:: Sample verification code can be found in the test code, in package ``il.ac.bgu.cs.bp.bpjs.verification.examples``.



.. _BPjs paper: https://export.arxiv.org/abs/1806.00842
.. _safety: https://en.wikipedia.org/wiki/Safety_property
.. _liveness: https://en.wikipedia.org/wiki/Liveness
.. _Live Sequence Charts: http://wiki.weizmann.ac.il/playgo/index.php/Live_sequence_charts