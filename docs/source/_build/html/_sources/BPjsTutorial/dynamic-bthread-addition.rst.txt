==============================
Adding B-Threads Dynamically
==============================

Let's count to 4 in an arbitrary order! That is, let's have 4 events, labeled ``e0`` to ``e3``, and not care about the order in which they occur. For this, we need four b-threads, each requesting a numbered event. But since they all do the same, we might want to create them from a loop, like so:

.. literalinclude:: code/dynamic-bthread-bad.js
  :linenos:
  :language: javascript

If we run this, however, we do not get what we want:

.. code:: bash
  :number-lines:

  $ java -jar BPjs.jar dynamic-bthread-bad.js
  #  [READ] /.../dynamic-bthread-bad.js
  -:BPjs Added requestor-0
  -:BPjs Added requestor-1
  -:BPjs Added requestor-2
  -:BPjs Added requestor-3
  #  [ OK ] dynamic-bthread-bad.js
  ---:BPjs Started
  --:BPjs Event [BEvent name:e4]
  ---:BPjs No Event Selected
  ---:BPjs Ended

This may seem weird: in lines 3 to 6, the value of ``i`` goes from 0 to 3, as expected. But there's only one event selected, and it's ``e4``.

The reason for this is that Javascript is a late-binding language, so ``i``'s  value is looked up as late as possible. For the logging, ``i`` has to be evaluated immediately, while the runtime builds a string to pass to the logger. The call to ``bp.Event("e"+i)``, on the other hand, is evaluated only when the b-thread is started.

Alas, the b-thread is started after the javascript file is evaluated. At this point, ``i`` is equal to 4. So in effect, we had four b-threads, named ``requestor-0`` to ``requestor-3``, all asking for event ``e4``.

To get four different events, we need to create a scope for each iteration. This is easy, since Javascript supports functions as first-class-citizens (:download:`source <code/dynamic-bthread-ok.js>`).

.. literalinclude:: code/dynamic-bthread-ok.js
  :linenos:
  :language: javascript

Lines 4-6 are the same as before, but now they are wrapped in an anonymous function (line 3) that's immediately called, with ``i`` as the parameter (line 7). This results in the b-thread function having its own scope (or, to be technically exact, activation object). That scope keeps the value of ``i`` at the iteration the scope was created in - that value is put in the parameter ``j``.

Thus, each b-thread gets its own scope and correct value of ``i`` (except that it's calling it ``j`` at this point).

.. code:: bash

  $ java -jar BPjs.jar dynamic-bthread-ok.js
  #  [READ] /.../dynamic-bthread-ok.js
  -:BPjs Added requestor-0
  -:BPjs Added requestor-1
  -:BPjs Added requestor-2
  -:BPjs Added requestor-3
  #  [ OK ] dynamic-bthread-ok.js
  ---:BPjs Started
  --:BPjs Event [BEvent name:e2]
  --:BPjs Event [BEvent name:e1]
  --:BPjs Event [BEvent name:e3]
  --:BPjs Event [BEvent name:e0]
  ---:BPjs No Event Selected
  ---:BPjs Ended
