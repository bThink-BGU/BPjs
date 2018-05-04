bp.registerBThread("GameEnd",function() {
	bsync({waitFor: [OWin, XWin, draw]});
	bsync({block: [X(0,0), X(0,1),...,O(2,2)]});
});
