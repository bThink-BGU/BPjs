==========================
Writing Programs in BPjs
==========================

BPjs is an environment for running Behavioral Programming programs written in Javascript. After completing this tutorial, you will be familiar with the basic concepts of Behavioral Programming, and will be able to write and run programs in BPjs.

What is Behavioral Programming?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Behavioral Programming, or *BP* for short, is based on scenarios. BP programs are composed of threads of behavior, called *b-threads*. B-threads run in parallel to coordinate a sequence of events via a synchronized protocol. During program execution, when a b-thread wants to synchronize with its peers, it submits a statement to the central event arbiter using the ``bp.sync`` statement.

A ``bp.sync`` statement declares which events the b-thread requests, which events it waits for (but does not request), and which events it would like to block (that is, prevent from being selected). Blocked and waited-for events can be described using a predicate or a list. Requested events have to be specified explicitly.

After calling ``bp.sync``, the b-thread is blocked, waiting for the rest of the b-threads to synchronize (call ``bp.sync``) as well. When all b-threads have submitted their statements, the arbiter selects an event that was requested but not blocked. It then wakes up the b-threads that requested or waited for that event. The rest of the b-threads remain blocked, until an event they requested or waited for is selected.

Behavioral Programming was introduced by Harel, Marron and Weiss in 2012 in a `paper`_ published at the *Communications of the ACM*.

.. note:: The ``bp.sync`` in BPjs expands on the "classic" ``bsync`` defined in the ACM paper: it contains an :doc:`interrupt <interrupts>` event set, and may contain a hint to the :doc:`event selection strategy <../extendBPjs/implement-ess>`.

.. _paper: http://cacm.acm.org/magazines/2012/7/151241-behavioral-programming/fulltext
.. _ACM Transactions on Computer Science: http://todo/fill/this


.. topic:: How come BPjs does not use ``bsync``?

  The classic BP literature uses the term `bsync` for behavioral synchronization between b-threads. BPjs used to have a ``bsync`` pseudo-keyword, but we replaced it with ``bp.sync``. This change packs almost everything BP under the ``bp`` object, and also makes it easier to synchronize from any code executed by the b-thread (including called functions). Equally important, is does not add a new keywords to Javascript.


Tutorial
~~~~~~~~
This tutorial will introduce you to BP principles and to the BPjs api. After completing it, you will be able to write BPjs programs. That's the technical part, though. Properly thinking in BP and in general scenario-based programming - well, that would probably take longer, as with any new programming paradigm.

.. toctree::
  :maxdepth: 2

  hello-world
  eventsets
  logging
  events-with-data
  interrupts
  dynamic-bthread-addition
  bp-object
