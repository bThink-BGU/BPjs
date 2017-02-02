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


The first b-thread requests a cake. Nothing much to note here, except the usage of a global variable (``CAKE_REQUEST``) to store an event shared between a few b-threads. Next:

.. literalinclude:: code/interrupts.js
  :linenos:
  :language: javascript
  :start-after: // Burn the cake
  :end-before: // Make me a cake

The "bad luck" b-thread waits for a ``"Bake Start"`` event. When this event is selected, it may or may not burn the cake. This is something the the "baker" b-thread has to protect itself against.

.. literalinclude:: code/interrupts.js
  :linenos:
  :language: javascript
  :start-after: // Make me a cake

The "baker" b-thread is a classic scenario, listing the staged of making a cake, once it's requested. Classic, except for the ``bsync`` at line 6, where the ``Cake Burnt`` event is declared to be interrupting. If, while waiting for the baking to complete, the cake burns, the baker terminates. Which is preferable to decorating and serving a burnt cake.

.. info :: Interrupting events do not add new capabilities to BP. The can be modeled by adding them as a ``waitFor`` parameter to ``bsync``, and then examining whether it is a member of the interrupting event set. Still, declaring event as *interrupting* adds declarative expressiveness (which, in turn, aids program analysis), and is more convenient.

Final Acts of an Interrupted B-Thread
--------------------------------------

.. todo :: Write about the interrupt handler.
