================
Events with Data
================

All events in BPjs have a name. Name alone can get you quite far, but sometimes some more data needs to be passed. For this reason, events also contain a ``data`` field, which holds a standard Javascript object. The event's name is available through its ``name`` field. Client code can initialize an event with data like so::

  var anEvent = bp.Event("answer", {value:42, scope:"everything"})
  anEvent.name

Adding data to events allows for a more complex interaction between b-threads. The b-threads in the example below collaborate in order to count to 10.
