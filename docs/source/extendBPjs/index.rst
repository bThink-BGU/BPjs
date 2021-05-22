===============
Extending BPjs
===============

Out of its proverbial box, BPjs enables "classic" behavioral programming: running multiple b-threads who coordinate through events. It's a start, and quite often it's enough. But sometimes one needs BPjs to behave a bit differently. This section of the manual discusses how to alter BPjs' default behavior to suite your specific needs.


.. tip:: If you develop an interesting or a useful extension, please consider making it available to others, e.g. by opening the source. Thanks!


.. toctree::
    :maxdepth: 2

    implement-ess
    interact-with-context


Sub-classing Events
-------------------

The built-in ``BEvent`` class can hold data in its name, and in a ``data`` field. However, sometimes the need arises for a more structured data storage, or one that's easier to create and read from the Java layer. To this end, it is possible to sub-class ``BEvent``.

.. warning::
    Event sub-classes MUST implement state-based ``equals()`` and ``hashCode()``, and implement ``java.io.Serializable``. Otherwise, event unification and program verification would not work.