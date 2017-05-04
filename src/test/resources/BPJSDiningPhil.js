/* global bp  */

A = bp.Event("A");
B = bp.Event("B");
C = bp.Event("C");
var r;

bp.registerBThread(function() {
	r=bsync({ waitFor : A }); // The problem is with this assignment.
	bsync({ waitFor : B });
	bsync({ waitFor : C });
});
