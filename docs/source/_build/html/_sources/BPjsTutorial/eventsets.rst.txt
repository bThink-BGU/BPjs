==========
Event Sets
==========

When a b-thread requests an event, it has to be specific. That is, the b-thread has to provide an event instance as a parameter to ``bp.sync``. This is not the case for waited-for and blocked events: there are specified by an **event set**. An event set can be specified in the following ways:

*  If the set contains a single event, the event itself serves as an event set. We used this in the :ref:`hello_world` example.

*  If the set contains some events known in advance, they can be passed as a value like so:

  .. code:: javascript

    bp.sync({waitFor:[evt1, evt2, evt3], block:[evt500]});

*  By an ``EventSet`` object. These have name and a predicate that takes an event and returns a boolean result: ``true`` if the event is a member of the set, and ``false`` otherwise:

  .. code:: Javascript

    bp.EventSet( "name", function(evt){
      return /* computation about evt goes here */;
    });

* By using one of the built-in event sets, ``bp.all``, which contains all events, or ``bp.none``, which is an empty set.

Example: Keep Serving Coffee
----------------------------

Consider a coffee shop that serves some teas, sparkling water, and -- surprise -- coffees. Customers come and request their drink. To make sure the coffee is always freshly ground, the ground coffee container is just large enough to contain amount needed for ten cups. Thus, after the tenth coffee, the baristas has to stop taking coffee orders and go grind some coffee. They can serve other orders, though. The code below (:download:`full source <code/eventsets.js>`) models this.

First off, we need to generate the orders. Nothing fancy here, just your usual event requesting for-loop.

.. literalinclude:: code/eventsets.js
  :linenos:
  :language: javascript
  :end-before: grind more coffee

The "orders" b-thread generates one hundred random orders. We now need to detect which orders are coffee orders, and then count them. We could list all the coffee order events in an array, but since their names end with "coffee", a more elegant solution would be using a predicate that detects this, and wrapping it in an ``EventSet``. This event set is defined in lines 2-4 in the listing below.

.. literalinclude:: code/eventsets.js
  :linenos:
  :language: javascript
  :start-after: grind more coffee


After defining the event set for coffee orders, the "coffee supply" b-thread loops forever, waiting for coffee order events (line 7). After ten orders, when it's time to grind more coffee, it blocks coffee orders from happening, and requests a coffee grinding event (the ``bp.sync`` at lines 10-11). Until the coffee grinding event is selected, no coffee order can be selected.

.. note:: Rather than counting how many coffees were made using some variable, the "coffee supply" b-thread loops for 10 times, waiting for coffee to be ordered. This is an example for the classic scenario style: X has to happen Y times, and then we do Z.

Here's a sample output of this b-program:

.. code:: bash

  $ java -jar BPjs.jar eventsets.js
  #  [READ] /.../eventsets.js
    -:BPjs Added orders
    -:BPjs Added coffee supply
  #  [ OK ] eventsets.js
  ---:BPjs Started
   --:BPjs Event [BEvent name:green tea]
   --:BPjs Event [BEvent name:green tea]
   --:BPjs Event [BEvent name:espresso coffee]
   --:BPjs Event [BEvent name:latte coffee]
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:espresso coffee]
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:espresso coffee]
   --:BPjs Event [BEvent name:latte coffee]
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:espresso coffee]
   --:BPjs Event [BEvent name:flatwhite coffee]
   --:BPjs Event [BEvent name:espresso coffee]
   --:BPjs Event [BEvent name:latte coffee]
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:espresso coffee]
   --:BPjs Event [BEvent name:Grind more coffee!]
   --:BPjs Event [BEvent name:flatwhite coffee]
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:black tea]
   ... continues ...

Creating Complex Behaviors Using Simpler Ones
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The coffee shop program also serves as an example for the BP way of composing complex behavior from simpler ones. Baristas serves drinks when they are ordered. They also grind coffee when the ground coffee supply is low. Using BP, these behaviors can be described separately by the programmer, so they are easy to maintain and reason about. At runtime, the BP execution engine -- BPjs, in our case -- combines them to the required complex behavior.
