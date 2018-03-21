==========
Logging
==========

As our programs become more complex, some logging/``printf`` capabilities are needed (sorry, no debugger yet). So, let's take a short break from Behavioral Programming in BPjs, and meet ``bp.log``.

``bp.log`` is a logger object. Logging is done to standard out (``System.out``, to be specific). There are four log levels, so while developing you can turn messages on and off, to help you focus on the problematic areas. The below program exercises the logger a bit:

.. literalinclude:: code/logging.js
  :linenos:
  :language: javascript

The "event generator" creates four events, one for each logging level. The "event logging" b-thread waits for all events (note the use of ``bp.all``), sets the logger level (line 14), and attempts to log in all three log methods (lines 15-17). Here is the program output. Note that after event 3, where the level is set to ``"Off"``, no messages are emitted at all.

.. code:: bash

  #  [READ] /.../logging.js
  [JS][Info] registering bthreads - start
    -:BPjs Added event generator
    -:BPjs Added event logging
  [JS][Info] registering bthreads - done
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





.. caution :: Later versions might integrate BPjs with a full-blown logging system, such as `logback`_ or `log4j2`_. Programs relying on the exact logging format may need to change once the logging is updated. If you need to write a program that relies on accurate interpretation a b-program life cycle and selected events, consider implementing a `BProgramListener`_.

.. _logback: https://logback.qos.ch
.. _log4j2: http://logging.apache.org/log4j/2.x/index.html
.. _BProgramListener: javadoc.io/page/com.github.bthink-bgu/BPjs/latest/il/ac/bgu/cs/bp/bpjs/bprogram/runtimeengine/listeners/BProgramListener.html
