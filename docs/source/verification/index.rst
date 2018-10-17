===============
Verification
===============

BPjs programs can be verified for conforming to a set of formal requirements. For example, it is possible to verify a b-program cannot get to a deadlock, or that a certain event is never selected. If, during verification, the verifier finds an example for violating the requirement being verified, it returns the trace of events that lead to the illegal situation. BPjs models are directly verified, meaning there's no model transformation involved.

BPjs models are verified using assertions. During execution, b-threads may call ``bp.ASSERT(cond, message)``. If ``cond`` evaluates to ``false``, the b-program is considered in violation of its requirements. Assertions can be used in runtime, but that's not their main goal. They are also used during traversal of the b-program (possibly infinite) state space.

.. tip:: ``message`` is an optional parameter explaining, in human-readable, terms what went wrong.


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

.. note:: Currently, only safety requirements are supported. Liveness requirements support is under active development.


From the Command Line
~~~~~~~~~~~~~~~~~~~~~

To verify a b-program from the commandline, simply add the ``--verify`` switch. Use ``--full-state-storage`` to force the verification to use the full state data when determining whether a state was visited (requires more memory).

  $ java -jar bpjs.jar --verify hello-possibly-broken-world.js


Assertions During Runtime
=========================

During normal execution (i.e. by a ``BProgramRunner``), failed assertion cause program execution to terminate. The execution BProgramRunner's listeners are notified of this failure. This approach is sometimes called "runtime verification", and is useful to implement "emergency shutdown" mechanisms.


.. note:: Sample verification code can be found in the test code, in package ``il.ac.bgu.cs.bp.bpjs.verification.examples``.



.. _BPjs paper: https://export.arxiv.org/abs/1806.00842
