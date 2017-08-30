/* global bp, bsync, PHILOSOPHER_COUNT */

bp.log.info('Tic-Tac-Toe - Let the game begin!');

var countX = 0;
var countO = 0;

//GameRules

/*
 * Block further marking of a square already
 * marked by X or O.
 */
bp.registerBThread("squareTaken", function() {
	while (true) {
		
		var e = bsync(
                {
					waitFor : [ bp.Event("Pick" + i + "R"),
							    bp.Event("Pick" + j + "L") ],
					block : [ bp.Event("Rel" + i + "R"),
							  bp.Event("Rel" + j + "L") ]
				}).name;

		var wt = (e.equals("Pick" + i + "R")) ? "Rel" + i + "R" : "Rel" + j	+ "L";
		// Request to pick the right stick
		bsync({
			request : bp.Event("Pick" + philNum + "R")
		});

	}
});

/*
 * Alternately block O moves while waiting
 * for X moves, and vice versa (we assume that X always plays first).
 */
bp.registerBThread("EnforceTurns", function() {
	while (true) {

	}
});

/*
 * Wait for placement of three X marks
 * in a line and request XWin.
 * To-do: Priority-------------------------------------
 */
bp.registerBThread("DetectXWin", function() {
	while (true) {
		bsync({
			waitFor : [ bp.Event("3XRow"),
					    bp.Event("3XCol") ],
						bp.Event("3XDia") ]
		}).name;

		bsync({
			request : bp.Event("XWin")
		});
	}
});

/*
 * Wait for placement of three O marks 
 * in a line and request OWin.
 * To-do: Priority-------------------------------------
 */
bp.registerBThread("DetectOWin", function() {
	while (true) {
		bsync({
			waitFor : [ bp.Event("3ORow"),
					    bp.Event("3OCol") ],
						bp.Event("3ODia") ]
		}).name;

		bsync({
			request : bp.Event("OWin")
		});
	}
});

/*
 * Wait for nine moves and request draw event.
 */
bp.registerBThread("DetectDraw", function() {
	while (true) {
		if(countX+countO == 9)
			
	}
});

//Tactics - DefaultOMoves






//Environment




