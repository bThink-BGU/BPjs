/* global bp, bsync, TicTacToeGameMain, Packages, isModelChecking */

// We expected to see 4 visited states, but only three are reported.

bp.registerBThread("STAM1", function() {
	while (true) {
		bsync({ request:[ bp.Event("STAM1") ] });
	}
});

bp.registerBThread("STAM2", function() {
	while (true) {
		bsync({ request:[ bp.Event("STAM2") ] });
	}
});
