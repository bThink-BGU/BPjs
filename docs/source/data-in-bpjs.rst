Data Storage in BPjs
=====================

Aside from variables, BPjs allows storing data object for each b-thread, and in a b-program-level map. While this is not a part of "classic" bp, it allows for some useful techniques which the BPjs community found useful.


B-Thread Data Object
--------------------

B-threads can store data in ``bp.thread.data``. This field is only visible to the storing b-thread. It is useful for storing information that is globally relevant to a b-thread in a given state. For example, if we want our b-thread to block certain events, we can store these events at the b-thread level::

    bp.thread.data = bp.EventSet("blocked", [eventA, eventB, eventC]);

Then, we can use our own synchronization function that also reads from the b-thread data field::

    function sync(stmt) {
        stmt.block = bp.thread.data;
        bp.sync(stmt);
    }

.. danger:: For verification to work, the data object stored in the b-thread has to implement state-based ``hashCode`` and ``equals``.


B-Program Data Store
--------------------

BP focuses on behaviors and events, and does not look so much into shared data storage. In particular, b-threads are only supposed to share data using events.
This works for the most part, but sometimes it is easier to be able to share data in other ways. The B-Program data store allows sharing data using a map, reachable from any thread using ``bp.store``.

The storage model for the b-program store is transactional. Changes made by a given b-thread are visible only to that b-thread, until is requests synchronization. When entering a synchronization point, BPjs tries to consolidate all storage modifications. If the consolidation succeeds, the changes are applied and will be visible to the rest of the b-threads at the next cycle. If the consolidation fails, the b-program enters a failed state, similar to the one it enters after an assertion fails.

.. note::
    
    Developers can react to b-program store modifications, as well as change them, and attempt to fix storage conflicts. This is done by implementing the ``StorageModificationStrategy`` interface, and registering it with a ``BProgram``. See ``StorageModificationStrategy`` javadoc for details.


B-Program Data Store API
........................

The b-program data store is a map, where the keys are strings, and the values can be any object. Available methods include:

    :put(k,v): Puts ``v`` (any object) in field ``k`` (which must be a string).
    :get(k): returns the value in ``k``, or ``null``.
    :has(k): returns ``true`` if the storage has a value under ``k``, and ``false`` otherwise.
    :remove(k): removes the value under ``k``, if any.
    :keys(): returns a set of all keys.
    :size(): returns the number of keys.




.. danger::
    For verification to work, the values in the b-program storage map should have state-based ``hashCode`` and ``equals``. This is the default for JavaScript objects. When using Java objects - especially custom ones - ensure these methods are implemented.
