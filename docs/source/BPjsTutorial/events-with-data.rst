================
Events with Data
================

All events in BPjs have a name. A name alone can get you quite far, but sometimes a programmer wants to send data in a more structured way, and without to- and from- string roundtrips. For this reason, events also contain a ``data`` field, which holds a standard Javascript object. The event's name is available through its ``name`` field. Client code can initialize an event with data like so::

  var anEvent = bp.Event("answer", {value:42, scope:"everything"});
  bp.log.info(anEvent.name); // logs "answer"
  bp.log.info(anEvent.data.value); // logs 42
  bp.log.info(anEvent.data.scope); // logs "everything"

Adding data to events allows for a more complex interaction between b-threads. The b-threads in the example (:download:`source <code/count-to-ten1.js>`) below collaborate in order run to count to 10.

The first b-thread requests three events: two with no data, and one with a ``counter`` type and a ``0`` value. Note that the use of ``type`` here is convention only; there's no static type system in place to enforce type correctness.

.. literalinclude:: code/count-to-ten1.js
  :linenos:
  :language: javascript
  :end-before: // Increasing the counter

The next b-thread is responsible for increasing the counter. Note the event set in charge of detecting counter events. Code accessing the ``data`` field of an event must use caution, as that field is often ``null``.

.. literalinclude:: code/count-to-ten1.js
  :linenos:
  :language: javascript
  :start-after: // Increasing the counter
  :end-before: // Capping

Finally, a third b-thread prevents the counter from reaching 10. Apart from the event set detecting counter events with ``value`` of ``10``, the body of the "Capper" b-thread consists of a single ``bp.sync`` which blocks these events. This is a common idiom in BP, in order to prevent something that "should never happen".

.. literalinclude:: code/count-to-ten1.js
  :linenos:
  :language: javascript
  :start-after: // Capping
