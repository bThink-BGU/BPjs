.. _installing_bpjs:

===============
Obtaining BPjs
===============

BPjs is available in multiple packagings. Selecting the which packaging is right for you depends on the way you plan to use it.

Running BPjs from the Commandline
---------------------------------

This is a .jar file containing all the dependencies needed for running BPjs, so it's the only file you need. Useful for running BPjs stand-alone. This file is available at our `release page`_, with the suffix ".uber.jar".

Using Maven
------------

BPjs is on Maven Central! Just add the following dependency to your ``<dependencies>`` section in ``pom.xml``::

  <dependency>
      <groupId>com.github.bthink-bgu</groupId>
      <artifactId>BPjs</artifactId>
      <version>[0.8.0, 0.9.9]</version>
  </dependency>


Using .jar Files
-----------------

BPjs is released using two .jar files, available at our `release page`_:

* ``BPjs-{version}.jar`` Contains only BPjs' classes. Use this if you want to manage the BPjs' dependencies yourself.

* ``BPjs-{version}.uber.jar`` Contains BPjs and its dependencies. Use this to execute BPjs programs, or if you are certain BPjs' dependencies are different from the dependencies of the rest of your project. Otherwise, classpath clashes might occur.


Compile from Source
--------------------

BPjs is built using `Apache Maven`_. Thus, you need to install Maven, or have an IDE that supports it. The BPjs team uses mostly `NetBeans`_, but Eclipse and IntelliJ IDEA should work too.

#. Download the sources from BPjs' `GitHub repo`_, either by cloning, or as a .zip file.

#. To build a regular .jar, use ``mvn package``

#. To build an "über" jar, containing all program dependencies, use ``mvn package -P uber-jar``

.. _release page: https://github.com/bThink-BGU/BPjs/releases
.. _Apache Maven: https://maven.apache.org
.. _NetBeans: http://netbeans.org
.. _GitHub repo: https://github.com/bThink-BGU/BPjs
