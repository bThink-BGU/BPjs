BPjs tips
=========

Mainly for commands that are useful, but not in frequent use.

Build jar with tests
--------------------

Useful for running long verifications outside of NetBeans

::

   mvn jar:test-jar

NOTE: The build must use jdk8 for now. Execution can be done on any jdk
(at least, worked for us with jdk11).

Running verifications that live in the tests directory from the terminal
------------------------------------------------------------------------

to run the actual test, also build the uber-jar:

::

   mvn package -P uber-jar

Now both live in the ``target`` directory. You can now run the test
using Java, as usual, with both jars in the ``-cp`` parameter:

::

   java -cp target/BPjs-0.9.2-SNAPSHOT.uber.jar:target/BPjs-0.9.2-SNAPSHOT-tests.jar il.ac.bgu.cs.bp.bpjs.TicTacToe.TicTacToeVerMain

Event comparison
----------------

Better to use non-strict JavaScript object comparison for now. So prefer

.. code:: javascript

     var evt = bp.sync({waitFor:ANY_ADDITION, block:chooseBlock(bias)});
     if ( evt.name == ADD_WETS.name ) {
       ...

over

.. code:: javascript

     var evt = bp.sync({waitFor:ANY_ADDITION, block:chooseBlock(bias)});
     if ( evt === ADD_WETS ) {
       ...

as the latter may return a false negative, especially during
verification.

Implementing Custom Events and Using Custom Objects as Event Data
-----------------------------------------------------------------

ALWAYS make sure that you have state-based, meaningful ``equals()`` and
``hashCode()``, and that serialization and de-serialization works. Or verification fails.

State minimization
------------------

(yes, this is informed by the “data minimization” directive of privacy
by design) It’s better to store least amount of data. E.g. in the
fruitRatio.js file, this version of the b-threads yields three states:

.. code:: javascript

   bp.registerBThread( "RaspberryAdder", function(){
       var fruitIndex=0;
       while (true) {
           var evt = null;
           if ( fruitIndex > 0 ) {
               evt = bp.hot(true).sync({request:ADD_RASPB,
                                        waitFor:ADD_FRUIT});
           } else {
               evt = bp.sync({waitFor:ADD_FRUIT});
           }
           fruitIndex = fruitIndex + 
                       evt.data.blueberries-evt.data.raspberries;
       }
   });

Where this version yields 4 (note different location of ``var evt``) :

.. code:: javascript

   bp.registerBThread( "RaspberryAdder", function(){
       var fruitIndex=0;
       var evt = null;
       while (true) {
           if ( fruitIndex > 0 ) {
               evt = bp.hot(true).sync({request:ADD_RASPB,
                                        waitFor:ADD_FRUIT});
           } else {
               evt = bp.sync({waitFor:ADD_FRUIT});
           }
           fruitIndex = fruitIndex + 
                       evt.data.blueberries-evt.data.raspberries;
       }
   });

That’s because the former does not store the event from the previous
iteration.

Variables Defined in Loops
--------------------------

When defining variables in loops, prefer ``let`` to ``const``. This is because ``const`` won't
change after the first iteration, and the assignment attempt will fail silently.

E.g. :

.. code:: javascript
    
    for ( let i=0; i<10; i++ ) {
        let evtName = bp.thread.data.eventPrefix + String(i);
        req(evtName); // convenience function for bp.sync({request... (non-standard)
    }

Will yield a series of events ``event-0`` to ``event-9``, whereas:

.. code:: javascript
    
    for ( let i=0; i<10; i++ ) {
        const evtName = bp.thread.data.eventPrefix + String(i);
        req(evtName); // convenience function for bp.sync({request... (non-standard)
    }

Will yield a series of 10 events named ``event-0``.

Working in Java with Objects from JavaScript
--------------------------------------------

When invoking methods on objects that come from Rhino, it is often required that these methods
are invoked in a context. This context can be obtained by calling ``BPjs.enterRhinoContext()``.
However, this method requires the client code to ensure that the context is closed properly.

An alternative would be to use the consumer pattern, with ``BPjs.withContext()``:

.. code:: java

    BProgram bprog = createBProgram(); // create b-program
    bprog.setup(); // run initial part
    JsEventSet es = bprog.getFromGlobalScope("esA", JsEventSet.class).get();
    
    // es now holds a JsEventSet, which includes a Rhino function,
    // and so, must be run within a context. 

    BPjs.withContext((c)->{
        assertTrue( es.contains(BEvent.named("AAA")));
    });

    