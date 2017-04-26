/*Works Good*/
P1R = bp.Event("Pick1R"); // Phil1 pickes right
P1L = bp.Event("Pick1L"); // Phil1 pickes left
P2R = bp.Event("Pick2R"); // Phil2 pickes right
P2L = bp.Event("Pick2L"); // Phil2 pickes left

R1L = bp.Event("Rel1L"); // Phil1 release left
R2L = bp.Event("Rel2L"); // Phil2 release left
R1R = bp.Event("Rel1R"); // Phil1 release right
R2R = bp.Event("Rel2R"); // Phil2 release right
RR1RR = bp.Event("RRel1RR"); // Phil2 release right

/*
 * bp.registerBThread("Phil1", function() { while (true) { bsync({ request : P1R
 * }); // requests to pick his right stick bsync({ request : P1L }); // requests
 * to pick his left stick
 * 
 * bsync({ request : R1L }); // requests to release his left stick bsync({
 * request : R1R }); // requests to release his right stick } });
 * 
 * 
 * bp.registerBThread("Phil2", function() { while (true) { bsync({ request : P2L
 * }); // requests to pick his left stick bsync({ request : P2R }); // requests
 * to pick his right stick
 * 
 * bsync({ request : R2R }); // requests to release his right stick bsync({
 * request : R2L }); // requests to release his left stick } });
 */

// Force the stick between p1 and p2 to be one
bp.registerBThread("Stick between P1 and P2", function() {
	var e;
	var wt;

	while (true) {
		// e = bsync({ waitFor : [ P1R, P2L ], block : [ R1R, R2L ] });
		e = bsync({ waitFor : P1R });  // The problem is with this assignment. It doesn't happen when we remove it ???

		// wt = (e.equals(P1R) ? R1R : R2L);

		bsync({ waitFor : R1R });
		// bsync({ waitFor : wt, block : [ P1R, P2L ] });
	}
});

/*
 * // Force the stick between p2 and p1 to be one bp.registerBThread("Stick
 * between P2 and P1", function() { var e; var wt;
 * 
 * while (true) { // Waits for pick up e = bsync({ waitFor : [ P1L, P2R ], block : [
 * R1L, R2R ] });
 * 
 * wt = (e.equals(P1L)) ? R1L : R2R; // Waits for release by the same Phil
 * bsync({ waitFor : wt, block : [ P1L, P2R ] }); } });
 */