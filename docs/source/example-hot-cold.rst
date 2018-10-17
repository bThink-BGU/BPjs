***********************
Example - Hot/Cold Bath
***********************

.. include example-hot-cold::

This program models a system that fills a bath with six parts water: three cold, and three hot. There are two faucets in the system: one for hot water and one for cold. The system defines an event for activating each of these faucets: ``HOT`` causes the hot faucet to release a single part of water, and
``COLD`` has a similar effect on the cold water faucet. Listing 1 shows a naive controller program: it registers a single b-thread that requests the events at an arbitrary order. For the purpose of this program, we assume
``HOT`` and ``COLD`` are defined outside of the b-program itself, as they have to be known to the faucet physical controllers; BPjs offers an easy way of achieving this, and so the listed code is realistic.

.. literalinclude:: Examples_code/HC_listing1.js
  :linenos:
  :language: javascript

Listing 1. A naive implementation of bath-filling controller.

The code in Listing 1 is not incorrect, but it also does not use the power of behavioral programming: it leaves no decision room for the event selection mechanism. Thus, when read as a specification, it is over-restrictive. An improved b-program is shown at Listing 2.

.. literalinclude:: Examples_code/HC_listing2.js
  :linenos:
  :language: javascript

Listing 2. A more lenient version of the bath-filling controller.

The code in Listing 2 consists of two b-threads: *add-hot*, which adds the hot water, and *add-cold*,  hich adds the cold water. This improves on the previous version in a number of ways. First, any order in which the water parts are added is supported, as the event selection mechanism of the b-program can choose between two events at most synchronization points.
Second, the program’s structure is now closer to the original program requirements, as it does not dictate the order in which the water are added to the bath. Lastly, naming the bthreads increases readability of code, model-checking results, and program logs. It also makes it easier to debug a program.

The code in Listing 2 may be a bit too lenient, though: it allows a scenarios where the bath becomes too hot or too cold while being filled (for example, when all *add_hot* runs to completion first, and adds the three part hot water before *add_cold* adds a single cold water part). This may be a problem if we want to, e.g., put a baby in the bath before it is full.

To prevent these unbalanced scenarios, we can add an additional b-thread, that will make sure the water temperature is balanced during the filling process. One such possible bthread is listed in Listing 3.

The *control-temp* b-thread ensures the temperature of the bath water is safe by blocking the addition of each part of hot water until a part of cold water is added. It is interesting to note that this b-thread can be added and removed without affecting the other b-threads. Thus, if there is a baby in the bath, we may (should?) choose to add this b-thread to the controller. Better yet, we can solve this within the b-program: add a ``bp.sync({waitFor:BABY_IN})`` statement at the beginning of control-temp, so it starts working only when a baby is in the bath.

.. literalinclude:: Examples_code/HC_listing3.js
  :linenos:
  :language: javascript

Listing 3. A b-thread that ensures safe water temperature by ensuring cold water are added before hot water are.

As a useful exercise, we invite the reader to further elaborate the hot-cold example, e.g. by supporting the insertion of the baby while filling the bath (which would require blocking of the ``BABY_IN`` event when the water temperature is too high), or by altering *control-temp* to be more permissive.
