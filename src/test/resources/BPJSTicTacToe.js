/* global bp, bsync, PHILOSOPHER_COUNT,TicTacToeGameMain */

importPackage(Packages.il.ac.bgu.cs.bp.bpjs.TicTacToe.events);

bp.log.info('Tic-Tac-Toe - Let the game begin!');

var SquareCount = 0;

// GameRules
//
// /*
// * Block further marking of a square already
// * marked by X or O.
// */
//

function addSquareBThreads(row, col) {

	bp.registerBThread("ClickHandler(" + row + "," + col + ")", function() {
		while (true) {

			if (!isModelChecking) {
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
			SquareCount++;
		}
	});
}

bp.registerBThread("EnforceTurns", function() {
	while (true) {
		bsync({
			waitFor : [ X(0, 0), X(0, 1), X(0, 2), X(1, 0), X(1, 1), X(1, 2), X(2, 0), X(2, 1), X(2, 2) ],

			block : [ O(0, 0), O(0, 1), O(0, 2), O(1, 0), O(1, 1), O(1, 2), O(2, 0), O(2, 1), O(2, 2) ]

		});

		bsync({
			waitFor : [ O(0, 0), O(0, 1), O(0, 2), O(1, 0), O(1, 1), O(1, 2), O(2, 0), O(2, 1), O(2, 2) ],

			block : [ X(0, 0), X(0, 1), X(0, 2), X(1, 0), X(1, 1), X(1, 2), X(2, 0), X(2, 1), X(2, 2) ]
		});
	}
});


//Player O defult strategy
bp.registerBThread("PlayerO", function() {
	while (true) {
		
		bsync({
			request : [ O(0, 0), O(0, 1), O(0, 2), O(1, 0), O(1, 1), O(1, 2), O(2, 0), O(2, 1), O(2, 2) ]			
		});
				
	}
});


for (var r = 0; r < 3; r++) {
	for (var c = 0; c < 3; c++) {
		addSquareBThreads(r, c);
	}
}

// bp.registerBThread("DetectDraw(" + row + "," + col + ")", function() {
// while (true) {
// if(SquareCount == 9)
// bp.log.info("Game Over!");
//
// }
// });
