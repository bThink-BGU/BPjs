.. _bprogram_lifecycle:

============================
B-Program Life Cycle in BPjs
============================

The life cycle of a b-program in BPjs is divided into two stages: program start and BP cycle. These are detailed below the diagram that describes them.

.. figure::  images/Lifecycle.png
   :align:   center

   B-Program's life cycle in BPjs. First, the Javascript program is executed and registers b-thread. When the Javascript program terminates, the BP cycle begins.


1. BPjs Program Start
---------------------

This stage is responsible for registering the b-threads that the b-program will start with (note that b-threads can be added by other b-threads during program execution). Technically, this stage consists of executing a Javascript program. This program registers b-threads by calling ``bp.registerBThread([name],b-thread-function)`` multiple times. The Javascript code is free to call any external resources it needs; for example, it can read a file or query a database, and register b-threads according to the results. When the Javascript code terminates, all registered b-threads are executed concurrently, until they reach their first synchronization point. Then, the next stage begins.

2. Classic BP-Cycle
-------------------

This stage is the classic behavioral programming cycle. In it, b-threads run in parallel. Each b-threads executes its own logic, until it decides to synchronize with its peers, by calling ``bp.sync({...})``. When all b-threads requested synchronization, the b-program is said to arrive at a *synchronization point*. Then, the BPjs runtime selects an event that was requested and not blocked. B-threads that requested or waited-for that event are resumed. The rest of the b-threads remain paused. They may be resumed at the next synchronization point, if an event they requested or waited-for is selected.


Environments and Interruptions
------------------------------

A b-program may remain at a synchronization point indefinitely, if there are no selectable events. This is not a bug -  in fact, it's one of the main features of BP, and here's why: A "stuck" b-program can still listen to external events, end resume active execution when an external event that is wait-for-ed by some b-threads occurs. This allows for b-programs to react to their environment in a natural way, and forms the foundation on which BP-based reactive systems are built upon.

When calling ``bp.sync()``, b-threads can also define which events makes them irrelevant. For example, if a b-thread should handle a ``FileReceived`` event, and instead the b-program receives a ``NoMoreFiles`` event from its environment, it is a bit pointless to keep that b-thread around, and it should be gracefully terminated. This feature is called *interrupting event*, and is covered :doc:`here <BPjsTutorial/interrupts>`.

Thus, we can slightly elaborate over the previous life cycle diagram:

.. figure::  images/Lifecycle-detailed.png
   :align:   center

   B-Program's life cycle in BPjs. First, the Javascript program is executed and registers b-thread. When the Javascript program terminates, all b-threads are executed concurrently until they reach their first synchronization point. Then, the BP cycle begins. During synchronization points, the BPjs runtime may accept events from the b-program's environment. In practice, this environment is often a host Java application (or any other JVM language, really).
   B-threads can define which events make them irrelevant. When an event that makes a b-thread irrelevant is selected, that b-thread is *interrupted*, and is removed from the b-program.
