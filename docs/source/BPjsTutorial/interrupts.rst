========================
Interrupting Event Set
========================

Sometimes, given an event, it's pointless to continue a certain behavior. For example, while preparing a cake, one needs to:

#. Buy the Ingredients

#. Mix them (it is a very simple cake)

#. Bake the mixture

#. Decorate the cake

#. Serve!

If the baking stage fails, it is pointless to decorate and serve, and so this entire behavior has to terminate.

In order to help modeling such scenarios, BPjs allows passing a set of *interrupting events* to ``bsync``. When an event that's a member of the interrupting event set of a given b-thread is selected, that b-thread is terminated. This is demonstrated in the code below (:download:`source <code/interrupts.js>`).

.. literalinclude:: code/interrupts.js
  :linenos:
  :language: javascript
  :end-before: // Burn the cake


The first b-thread requests a cake. Nothing much to note here, except the usage of a global variable (``CAKE_REQUEST``) to store an event shared between a few b-threads. Let's look at the oven:

.. literalinclude:: code/interrupts.js
  :linenos:
  :language: javascript
  :start-after: // Burn the cake
  :end-before: // Make me a cake

The "Oven" b-thread waits for a ``"Bake Start"`` event. When this event is selected, it starts baking the cake - but has a 50% chance of burning it. This is something the the "baker" b-thread has to protect itself against.

.. literalinclude:: code/interrupts.js
  :linenos:
  :language: javascript
  :start-after: // Make me a cake

The "baker" b-thread is a classic scenario, listing the stages of making a cake, once it's requested. Classic, except for the ``bsync`` at line 6, where the ``Cake Burnt`` event is declared to be interrupting. If, while waiting for the baking to complete, the cake burns, the baker terminates. Which is preferable to decorating and serving a burnt cake.

.. note:: Interrupting events do not add new capabilities to BP. The can be modeled by adding them as a ``waitFor`` parameter to ``bsync``, and then examining whether it is a member of the interrupting event set. Still, declaring event as *interrupting* adds declarative expressiveness (which, in turn, aids program analysis), and is more convenient.

Here's the output of an unsuccessful baking attempt:

.. code:: bash

  $ java -jar BPjs.jar interrupts.js
  #  [READ] /.../interrupts.js
    -:BPjs Added cake request
    -:BPjs Added Oven
    -:BPjs Added baker
  #  [ OK ] interrupts.js
  ---:BPjs Started
   --:BPjs Event [BEvent name:Cake]
   --:BPjs Event [BEvent name:Buy Ingredients]
   --:BPjs Event [BEvent name:Mix Ingredients]
   --:BPjs Event [BEvent name:Bake Start]
   --:BPjs Event [BEvent name:Cake Burnt]
    -:BPjs Removed baker
  ---:BPjs No Event Selected
  ---:BPjs Ended


Final Acts of an Interrupted B-Thread
--------------------------------------

A b-thread can specify a handler function for interrupting events. This functions gets the interrupting event as a parameter.

The function can be used for clean up and logging, but as it is *not executed as a b-thread*, it **cannot call ``bsync``**. It can, however, enqueue events externally. Let's revisit the last example, and add enqueue a "sorry, no cake" event to inform the customer (:download:`source <code/interrupts-handler.js>`).

.. literalinclude:: code/interrupts-handler.js
  :linenos:
  :language: javascript
  :start-after: // Make me a cake

Lines 2-6 of the baker b-thread set a handler for handling the unfortunate event of the burnt cake. The handler first logs why the b-thread was interrupted (line 3), and then enqueues two events to declare that no cake will be served (lines 4-5).

.. note:: External events are polled only when there are live b-threads in the b-program. If all b-threads terminate while the external event queue contains events, there events will never be selected.

.. tip:: Enqueueing external events can also be done from regular b-threads. This can serve as a sort of asynchronous event request. Note that events requested this way may never be selected, even if they were not blocked (see previous note).
