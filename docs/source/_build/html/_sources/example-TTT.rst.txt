*********************
Example - Tic Tac Toe
*********************

.. include example-TTT::

This example is borrowed, with modifications, from D. Harel, A. Marron, and G. Weiss - “Programming coordinated scenarios in java”. 
Its main feature is that it presents the  concept of aligning bthreads to requirements. Meaning, each b-thread represents a rule, or a part of the tactics in the game of Tic-Tac-Toe. 

First, let us describe the (classical) game of Tic-Tac-Toe, and the events that represent the expected behaviors. Two players, X and O, alternately mark squares on a 3 X 3 grid whose squares are identified by (row, column) pairs: (0, 0), (0, 1), . . . ,(2, 2). The winner is the player who manages to form a full horizontal, vertical, or diagonal line with three of his/her marks. If the entire grid becomes marked but no player has formed a line, the result is a draw. Below, we assume player X is played by a human user, and player O is played by the application.

The goal of the programmer here is to implement the tactics for the O player such that the computer never loses. To check this main requirement, we use our model-checking tool that we will present soon. The game rules translate into b-threads as follows:

.. literalinclude:: Examples_code/TTT_listing1.js
  :linenos:
  :language: javascript

``Listing 1. A b-thread that implements the requirement that X and O play alternatively.``

.. literalinclude:: Examples_code/TTT_listing2.js
  :linenos:
  :language: javascript

``Listing 2. A b-thread that implements the requirement that a square may only be marked once. Given the variables row and col that represent the coordinates of a square on the grid, the b-thread waits for a move on that square and blocks further moves on the same square.``

.. literalinclude:: Examples_code/TTT_listing3.js
  :linenos:
  :language: javascript

``Listing 3. A b-thread that implements the draw ending condition. It waits for nine moves and then requests to announce a draw. The variable move is a representation of all events of type Move. The number 90 specifies the priority of the request.``

.. literalinclude:: Examples_code/TTT_listing4.js
  :linenos:
  :language: javascript

``Listing 4. A b-thread that implements the X winning condition. Given a permutation p and a line l (row, column, or diagonal), the b-thread waits for three X events on the line, in the order specified by the permutation, and then requests to announce that X wins the game.``

.. literalinclude:: Examples_code/TTT_listing5.js
  :linenos:
  :language: javascript

``Listing 5. A b-thread that implements the O winning condition. Given a permutation p and a line l (row, column, or diagonal), the b-thread waits for three O events on the line, in the order specified by the permutation, and then requests to announce that O wins the game.``

.. literalinclude:: Examples_code/TTT_listing6.js
  :linenos:
  :language: javascript

``Listing 6. A b-thread that implements the detection of user’s (player X) click.``

.. literalinclude:: Examples_code/TTT_listing7.js
  :linenos:
  :language: javascript

``Listing 7. A b-thread that implements the requirement that no moves are allowed once the game ends.``

We now present the main part of the specification: a strategy for player O implemented by b-threads. While there are many implementations of strategies for this game, our approach here is to break the strategy to elements that correspond to the way parents teach their children how to win (or, at least, avoid losing) the game. Arguably, we claim that people do not usually use the minimax algorithm that most computers are programmed to apply. Instead, we argue that most people use some set of intuitive rules of thumb. An example of a set of such rules is modeled by the b-threads below.

.. literalinclude:: Examples_code/TTT_listing8.js
  :linenos:
  :language: javascript

``Listing 8. A b-thread that implements the thumb-rule that, if no other thumbrule applies, it is best to put an O in the center square.``

.. literalinclude:: Examples_code/TTT_listing9.js
  :linenos:
  :language: javascript

``Listing 9. A b-thread that implements the thumb-rule that, if no other thumbrule applies, and the center square is taken, it is best to put an O in a corner square.``

.. literalinclude:: Examples_code/TTT_listing10.js
  :linenos:
  :language: javascript

``Listing 10. A b-thread that implements the thumb-rule that, if no other thumbrule applies, and the center and all corner squares are taken, put an O in a side square.``

Note that the requests of the last three b-threads are with priorities 35, 20, and 10, respectively. These are the lowest priorities among all the b-threads that implement the thumbrules. This means that the event selection mechanism will only obey these requests if no other thumb-rule applies. It also means that we prefer to use the center over corners and corners over sides.
We proceed to describe the thumb-rules that relate to scenarios in the game:

.. literalinclude:: Examples_code/TTT_listing11.js
  :linenos:
  :language: javascript

``Listing 11. A b-thread that implements the thumb-rule of putting an O in a line with two other O’s, in order to win the game. Given a permutation p and a line l (row, column, or diagonal), the b-thread waits for two O events on the line, in the order specified by the permutation, and then requests to mark its final O.``

Note that the priority of the *AddThirdO* b-thread is higher than the priority of *PreventThirdX*. This is because we prefer to win a game if possible.

.. literalinclude:: Examples_code/TTT_listing12.js
  :linenos:
  :language: javascript

``Listing 12. A b-thread that implements the thumb-rule of putting an O in a line with two X’s, in order to prevent a win of player X in the next move. Given a permutation p and a line l (row, column, or diagonal), the b-thread waits for two X events on the line, in the order specified by the permutation, and then requests to mark an O on the third square.``

The last type of thumb-rules in our strategy handle the so called “fork situations”, when player X tries to complete two lines at the same time. We only list one of them here, the *PreventFork00X* b-thread, that identifies one of the three ‘fork situations’. There are three more similar b-threads to handle the other similar situations.

.. literalinclude:: Examples_code/TTT_listing13.js
  :linenos:
  :language: javascript

``Listing 13. A b-thread that implements the thumb-rule of preventing from player X to complete two lines at the same time. Given a permutation p and a “fork situation line” f (row or column), the b-thread waits for two X events on the line, in the order specified by the permutation, and then requests to mark an O on one of a given set of squares.``

.. literalinclude:: Examples_code/TTT_listing14.js
  :linenos:
  :language: javascript

``Listing 14. A b-thread that implements the thumb-rule of preventing player X from completing two lines at the same time using one of the diagonals. Given a permutation p and a “fork situation diagonal’ f, the b-thread waits for two X events on the diagonal, in the order specified by the permutation, and then requests to mark an O on one of a given set of squares.``


Notice that the b-threads in listings 3-5 and 20-26 involve the priority option so the application can best detect the situation its facing. For example, in listing 3-5, the DetectXWin and DetectOWin b-threads have priority 100 to ensure that the application detects these before it detects a draw. 
Also, in listing 11, the priority of *AddThirdO* is higher than that of *PreventThirdX* because we want the application to prefer to win the game over a draw, or worse, giving the user (player X) another possibility to win the game in the next round (in case of a fork situation). The priority number is passed as an additional data to the ``bp.sync`` request. The additional data field is a general mechanism that can be used to attach meta-tdata, such as priorities, to synchronization statements. This data can, as done here, be used by the event selection mechanism to guide its selections.

The Tic-Tac-Toe example shows that it is possible to maintain an intuitive one-to-one relation between requirements and b-threads. It also demonstrates the usage of a customized 
event selection strategy, that takes priorities into account when selecting events.

.. figure::  images/TTT_fig1.png
   :align:   center

``Fig. 1. BPjs program stack, used for b-program execution. Parts that can be provided by client code appear in white. The behavioral code (written in JavaScript) is at the top level. This code can interact with its BPjs infrastructure using a special object exposed by BPjs, called bp. BPjs runs the standard JavaScript parts of the b-program code using the Mozilla Rhino JavaScript engine. Event selection is done using an event selection strategy object. When custom event selection logic is required, the host Java application can provide a custom EventSelectionStrategy instance to BPjs. The host application can interact with BPjs and the program it currently executes using an API, and by pushing events to a queue. It can listen to event selections and other b-program life cycle events by providing a listener object to the BProgramRunner running the b-program.``

Note that the priority event selection mechanism in BPjs is pluggable. Thus, programmers can implement and use other types of prioritization schemes instead of the default event selection strategy, which uses a random arbiter.

