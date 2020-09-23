====================
Packages and Classes
====================

BPjs is divided into three main parts, following from its leading principle that b-programs are really models, which can be executed just as well as analysed. Thus, the package structure (shown below) is divided into:

``model``
    Contains core BP concepts, such as BProgram and BEvent.

``analysis``
    Contains classes required for analyzing a b-program.

``execution``
    Contains classes for executing b-programs.


.. figure:: images/class-diagram.png
    :align: center

    Main classes and packages of BPjs.


BPjs contains other packages as well, but they are technical in nature:

``bprogramio``
    Deals with storing and loading b-program state.

``mains``
    Command-line applications and utilities. Some of the classes in this package may grow up to become full-fledged applications, and leave to their own repository.

``exceptions``
    Holds exceptions related to BPjs.
