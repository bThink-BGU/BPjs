P1R = bp.Event("Pick1R"); // Phil1 picks right
P1L = bp.Event("Pick1L"); // Phil1 picks left
P2R = bp.Event("Pick2R"); // Phil2 picks right
P2L = bp.Event("Pick2L"); // Phil2 picks left

R1R = bp.Event("Rel1R"); // Phil1 release right
R1L = bp.Event("Rel1L"); // Phil1 release left
R2R = bp.Event("Rel2R"); // Phil2 release right
R2L = bp.Event("Rel2L"); // Phil2 release left

bp.registerBThread("Phil1", function() {
	while (true) {
		// Request to pick the right stick
		bsync({ request : P1R });

		// Request to pick the left stick
		bsync({ request : P1L });

		// Request to release the left stick
		bsync({ request : R1L });

		// Request to release the right stick
		bsync({ request : R1R });
	}
});

bp.registerBThread("Phil2", function() {
	while (true) {
		// Request to pick the right stick
		bsync({ request : P2L });

		// Request to pick the left stick
		bsync({ request : P2R });

		// Request to release the left stick
		bsync({ request : R2R });

		// Request to release the right stick
		bsync({ request : R2L });
	}
});

bp.registerBThread("Stick between P1 and P2", function() {
	while (true) {
		var e = bsync({ waitFor : [ P1R, P2L ], block : [ R1R, R2L ] }).name; 

		var wt = (e.equals(P1R.name)) ? R1R : R2L;
		bsync({ waitFor : wt, block : [ P1R, P2L ] });  
	}
});

bp.registerBThread("Stick between P2 and P1", function() {
	while (true) {
		var e = bsync({ waitFor : [ P1L, P2R ], block : [ R1L, R2R ] }).name; 

		var wt = (e.equals(P1L.name)) ? R1L : R2R;
		bsync({ waitFor : wt, block : [ P1L, P2R ] });
	}
});
