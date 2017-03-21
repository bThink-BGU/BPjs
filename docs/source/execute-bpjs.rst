Executing BPjs Programs
=======================

From the Commandline
---------------------

You'll need:

#. `Java`_
#. BPjs's über-jar (the jar that contains BPjs and its dependencies). See :doc:`install-bpjs`.
#. Your code files

Once you have all of the above, running your BPjs programs is easy::

  java -jar <path-to-uber-jar> <your-code-1>.js <your-code-2>.js ... <your-code-n>.js

Replace <path-to-uber-jar> with the actual path to the uber jar file, <your-code-1>.js to the path to the actual code file, etc.

An alternative to using code files is having some program send the code to BPjs via ``stdin``. This is useful, e.g., if you have a program that generates BPjs code from a model, or if you've copied some code to the clipboard. In order to have BPjs read code from its ``stdin``, add a dash (``-``) to the list of source files.

The example below executes the code from f1.js and the clipboard. ``pbpaste`` is a utility program (on Macs) that pushes the clipboard content to ``stdout``::

  pbpaste | java -jar <path-to-uber-jar> f1.js -


From Java Code
---------------------

.. todo:: write this


.. _Java: https://www.java.com/en/
