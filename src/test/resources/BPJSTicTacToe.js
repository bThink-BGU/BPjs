/* global bp, bsync, PHILOSOPHER_COUNT,TicTacToeGameMain */

importPackage(Packages.il.ac.bgu.cs.bp.bpjs.TicTacToe.events);

bp.log.info('Tic-Tac-Toe - Let the game begin!');

var countX = 0;
var countO = 0;

// //GameRules
//
// /*
// * Block further marking of a square already
// * marked by X or O.
// */
//

function addSquareBThreads(row, col) {

	
	bp.registerBThread("ClickHandler(" + row + "," + col + ")", function() {
		while (true) {
			
			if( ! isModelChecking ) {
				bsync({
					waitFor : [ Click(row, col) ]
				});
			}
			
			bsync({
				request : [ X(row, col) ]
			});
		}
	});

	bp.registerBThread("SquareTaken(" + row + "," + col + ")", function() {
		while (true) {
			bsync({
				waitFor : [ X(row, col), O(row, col) ]
			});
			bsync({
				block : [ X(row, col), O(row, col) ]
			});
		}
	});

}

for (var r = 0; r < 3; r++) {
	for (var c = 0; c < 3; c++) {
		addSquareBThreads(r, c);
	}
}

//
// /*
// * Alternately block O moves while waiting
// * for X moves, and vice versa (we assume that X always plays first).
// */
// bp.registerBThread("EnforceTurns", function() {
// while (true) {
//
// }
// });
//
// /*
// * Wait for placement of three X marks
// * in a line and request XWin.
// * To-do: Priority-------------------------------------
// */
// bp.registerBThread("DetectXWin", function() {
// while (true) {
// bsync({
// waitFor : [ bp.Event("3XRow"),
// bp.Event("3XCol"),
// bp.Event("3XDia") ]
// }).name;
//
// bsync({
// request : bp.Event("XWin")
// });
// }
// });
//
// /*
// * Wait for placement of three O marks
// * in a line and request OWin.
// * To-do: Priority-------------------------------------
// */
// bp.registerBThread("DetectOWin", function() {
// while (true) {
// bsync({
// waitFor : [ bp.Event("3ORow"),
// bp.Event("3OCol"),
// bp.Event("3ODia") ]
// }).name;
//
// bsync({
// request : bp.Event("OWin")
// });
// }
// });
//
// /*
// * Wait for nine moves and request draw event.
// */
// bp.registerBThread("DetectDraw", function() {
// while (true) {
// if ( countX+countO === 9 ) {
// // TODO what?
// }
//			
// }
// });
//
// //Tactics - DefaultOMoves
//
//
//
//
//
//
// //Environment
//
//
//
//
