==========
Event Sets
==========

When a b-thread requests an event, it has to be specific. That is, the b-thread has to provide an event instance as a parameter to ``bp.sync``. This is not the case for waited-for and blocked events: these are specified by an **event set**. An event set contains other events. It is a set in the mathematical sense - it does not have to store the events it contains in a data structure, just to answer *yes* or *no* when asked whether it contains them. Accordingly, an event set is defined by a function that accepts an event and returns ``true`` or ``false``.

An event is a special type of event set - an event set that contains only itself. So we get that:

.. code:: javascript

  const eventA = bp.Event("A");
  const eventB = bp.Event("B");

  eventA.contains(eventA); // true
  eventA.contains(eventB); // false

An event set can be specified in the following ways:

*  If the set contains a single event, the event itself serves as an event set. We used this in the :ref:`hello_world` example.

* By composing events through their composition methods ``or``, ``and``, ``negate``, ``xor``, ``nor``, and ``nand``:

  .. code:: javascript

    const aOrB = bp.Event("A").or( bp.Event("B") ); // contains events "A" and "B"
    const notA = bp.Event("A").negate(); // all events except "A"
    const notAOrB = bp.Event("A").or( bp.Event("B") ).negate(); // contains all events 
                                                                // except for "A" and "B"

* By using the ``bp.eventSets`` utility object:

  .. code:: javascript

    const aOrB = bp.eventSets.anyOf( bp.Event("A"), bp.Event("B") );
    const conj = bp.eventSets.allOf( es1, es2 ); // events that are members of event sets ``es1`` and ``es2``.
    const notA = bp.eventSets.not( bp.Event("A") ); // all events except "A".

*  By an ``EventSet`` object. These have name and a predicate that takes an event and returns a boolean result: ``true`` if the event is a member of the set, and ``false`` otherwise. This way of defining event sets is verbose, but allows for fine-grained control over the set containment logic:

  .. code:: Javascript

    bp.EventSet( "eventsWhoseNameStartsWithA", function(evt){
      return (evt.name.indexOf("A")==0);
    });


*  An array of event sets is a translated to a disjunction (or) of those events (however, this is an old form which is not encouraged anymore, since it's not intuitive whether the semantics are of AND or of an OR).:

  .. code:: javascript

    bp.sync({waitFor:[evt1, evt2, evt3], block:evt500}); // same as evt1.or(evt2).or(evt3)

.. tip::
  The utility object ``bp.eventSets`` has two useful event set constants: ``bp.eventSets.all``, which contains all events, and ``bp.eventSets.none``, which is an empty set.

Example: Keep Serving Coffee
----------------------------

Consider a coffee shop that serves some teas, sparkling water, and -- surprise -- coffees. Customers come and request their drink. To make sure the coffee is always freshly ground, the ground coffee container is just large enough to contain the amount needed for ten cups. Thus, after the tenth coffee, the baristas has to stop taking coffee orders and go grind some coffee. They can serve other orders, though. The code below (:download:`full source <code/eventsets.js>`) models this.

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

.. danger:: At the moment, JavaScript arrow functions are only partially supported for event sets; They work great when running, but might break verification. Until this gets fixed in Rhino, we recommend using old-school functions for event sets (and everywhere else when writing BPjs code).

After defining the event set for coffee orders, the "coffee supply" b-thread loops forever, waiting for coffee order events (line 7). After ten orders, when it's time to grind more coffee, it blocks coffee orders from happening, and requests a coffee grinding event (the ``bp.sync`` at lines 10-11). Until the coffee grinding event is selected, no coffee order can be selected.

.. note:: Rather than counting how many coffees were made using some variable, the "coffee supply" b-thread loops for 10 times, waiting for coffee to be ordered. This is an example for the classic scenario style: X has to happen Y times, and then we do Z.

Here's a sample output of this b-program (annotated):

.. code:: bash

  $ java -jar BPjs.jar eventsets.js
  #  [READ] /.../eventsets.js
    -:BPjs Added orders
    -:BPjs Added coffee supply
  #  [ OK ] eventsets.js
  ---:BPjs Started
   --:BPjs Event [BEvent name:green tea]
   --:BPjs Event [BEvent name:green tea]
   --:BPjs Event [BEvent name:espresso coffee]  «1»
   --:BPjs Event [BEvent name:latte coffee]     «2»
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:espresso coffee]  «3»
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:espresso coffee]  «4»
   --:BPjs Event [BEvent name:latte coffee]     «5»
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:espresso coffee]  «6»
   --:BPjs Event [BEvent name:flatwhite coffee] «7»
   --:BPjs Event [BEvent name:espresso coffee]  «8»
   --:BPjs Event [BEvent name:latte coffee]     «9»
   --:BPjs Event [BEvent name:black tea]
   --:BPjs Event [BEvent name:espresso coffee]  «10»
   --:BPjs Event [BEvent name:Grind more coffee!] «**Grind**»
   --:BPjs Event [BEvent name:flatwhite coffee] «11»
   --:BPjs Event [BEvent name:sparkling water]
   --:BPjs Event [BEvent name:black tea]
   ... continues ...

Creating Complex Behaviors Using Simpler Ones
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The coffee shop program also serves as an example for the BP way of composing complex behavior from simpler ones. Baristas serves drinks when they are ordered. They also grind coffee when the ground coffee supply is low. Using BP, these behaviors can be described separately by the programmer, so they are easy to maintain and reason about. At runtime, the BP execution engine -- BPjs, in our case -- combines them into the required complex behavior.
