==========
Logging
==========

As our programs become more complex, some logging/``printf`` capabilities are needed (sorry, no debugger yet). So, let's take a short break from Behavioral Programming in BPjs, and meet ``bp.log``.

``bp.log`` is a logger object. Logging is done to standard out (``System.out``, to be specific). There are four log levels, so while developing you can turn messages on and off, to help you focus on the problematic areas. The below program exercises the logger a bit:

.. literalinclude:: code/logging.js
  :linenos:
  :language: javascript

The "event generator" creates four events, one for each logging level. The "event logging" b-thread waits for all events (note the use of ``bp.all``), sets the logger level (line 14), and attempts to log in all three log methods (lines 15-17). Here is the program output. Note that after event 3, where the level is set to ``"Off"``, no messages are emitted at all.

.. code:: 

  #  [READ] /.../logging.js
  [JS][Info] registering b-threads - start
    -:BPjs Added event generator
    -:BPjs Added event logging
  [JS][Info] registering b-threads - done
  #  [ OK ] logging.js
  ---:BPjs Started
   --:BPjs Event [BEvent name:event 0]
  [JS][Warn] event 0
  [JS][Info] event 0
  [JS][Fine] event 0
   --:BPjs Event [BEvent name:event 1]
  [JS][Warn] event 1
  [JS][Info] event 1
   --:BPjs Event [BEvent name:event 2]
  [JS][Warn] event 2
   --:BPjs Event [BEvent name:event 3]
  ---:BPjs No Event Selected
  ---:BPjs Ended


.. caution :: Later versions might integrate BPjs with a full-blown logging system, such as `logback`_ or `log4j2`_. Programs relying on the exact logging format may need to change once the logging is updated. If you need to write a program that relies on accurate interpretation a b-program life cycle and selected events, consider implementing a `BProgramRunnerListener`_.

.. note::
  Logging does not have to go to ``System.out``. Client code can set its destination ``PrintStream`` by calling ``BProgram#setLoggerOutputStreamer``.

Message Formatting
------------------

The BPjs logger formats messages using Java's `MessageFormat`_. Under the hood, JavaScript objects are printed using a special formatter, which gives more information that the default cryptic ``[object object]``. The code below contains some formatting examples:

.. literalinclude:: code/logging-with-data.js
  :linenos:
  :language: javascript


.. code:: 

  [BP][Info] Here is field hello: World of object {JS_Obj hello:"World", idioms:[JS_Array 0:"request" | 1:"waitFor" | 2:"block"]}
  [BP][Info] Here is are some stuff: {JS_Set "thing 1", "thing 2", "thing 42"}
  [BP][Info] I have a 1,000,000 reasons to block this event.
  [BP][Info] 3.142 3.14 3.1416


Caution - Array Ambiguity
~~~~~~~~~~~~~~~~~~~~~~~~~

A curious API edge-base ocurres when using message formatting, and passing a single variable for printing, AND that single variable is an array. The system confuses that array for the variable argument number, and only the first item of the array is printed. So the following code:

.. code::

  bp.registerBThread("t1",function(){
    bp.log.info("array:{0}", ["x","y","z"]);
  });

Prints this::

[BP][Info] array:x 

To work around this, either include a dummy variable, or wrap the array in another array::

  bp.log.info("array:{0}", [["x","y","z"]]);
  bp.log.info("array:{0}", ["x","y","z"], "dummy val");




.. _logback: https://logback.qos.ch
.. _log4j2: http://logging.apache.org/log4j/2.x/index.html
.. _BProgramRunnerListener: http://javadoc.io/page/com.github.bthink-bgu/BPjs/latest/il/ac/bgu/cs/bp/bpjs/execution/listeners/BProgramRunnerListener.html
.. _MessageFormat: https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html