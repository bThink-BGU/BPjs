bp.registerBThread("GameEnd",function() {
	bp.sync({waitFor: [OWin, XWin, draw]});
	bp.sync({block: [X(0,0), X(0,1),...,O(2,2)]});
});
