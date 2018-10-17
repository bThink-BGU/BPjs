================================
Welcome
================================

BPjs is a library for executing behavioral programs written in Javascript. It can
be used from the commandline, or embedded in a Java application.

BPjs is an open source project, maintained by a research group in Ben-Gurion
University of the Negev, Israel. The code is `available on GitHub`_.

.. _available on GitHub: https://github.com/bThink-BGU/BPjs

Here you can learn about :doc:`writing <BPjsTutorial/index>` and :doc:`executing <execute-bpjs>` BPjs programs,
:doc:`embedding <embed-bpjs>` BPjs programs in larger Java systems (or any JVM-based systems, really),
and extending and modifying BPjs' behavior.

What Do You Want to Do
----------------------

* **Learn Behavioral Programming:** If you are new to Behavioral Programming (that's alright, it's a fairly new concept), you're invited to read the :doc:`quick intro <BPjsTutorial/index>`.
* **Use BPjs from a console/commandline:** see :ref:`Running BPjs from the Commandline <running_cmd_line>`.
* **Use BPjs as a library in a software system:** If you're using `maven`_, just add BPjs `as a dependency`_. Otherwise, you can `download a .jar file`_.
* **Verify BPjs Programs/Models** see :doc:`Verification <verification/index>`.
* **Learn more about how to use, embed, and extend BPjs:** Read below.



.. _maven: http://maven.apache.org
.. _as a dependency: https://github.com/bThink-BGU/BPjs#getting-bpjs
.. _download a .jar file: https://github.com/bThink-BGU/BPjs/releases

.. todo: add structure of a bprogram diagram, event queue, listeners etc.

Topics:
-------

.. toctree::
   :maxdepth: 2

   install-bpjs
   BPjsTutorial/index
   execute-bpjs
   embed-bpjs
   verification/index
   extendBPjs/index
   glossary
   examples



Indices and tables
==================

* :ref:`genindex`
* :ref:`search`
