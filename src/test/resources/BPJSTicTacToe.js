/* global bp, bsync, PHILOSOPHER_COUNT,TicTacToeGameMain */

importPackage(Packages.il.ac.bgu.cs.bp.bpjs.TicTacToe.events);

bp.log.info('Tic-Tac-Toe - Let the game begin!');

var SquareCount = 0;

//GameRules
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
			SquareCount++;
		}
	});

	bp.registerBThread("EnforceTurns(" + row + "," + col + ")", function() {
		while (true) {
			var e = bsync({
				waitFor : [ X(row, col), O(row, col) ]
			});
			
			var wt = (e.equals(X(row, col))) ? O(row, col) : X(row, col) ;

			bp.log.info("It's "+ wt.name + "Turn");
			
			bsync({
				block : [ bp.Event(wt) ]
			});
			
			delete wt;
			delete e;
		}
	});
	
	
}

for (var r = 0; r < 3; r++) {
	for (var c = 0; c < 3; c++) {
		addSquareBThreads(r, c);
	}
}

//bp.registerBThread("DetectDraw(" + row + "," + col + ")", function() {
//	while (true) {
//		if(SquareCount == 9)
//			bp.log.info("Game Over!");
//
//	}
//});
