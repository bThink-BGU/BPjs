bp.registerBThread("PreventThirdX", function() {
	while (true) {
		bsync({waitFor:[ X(l[p[0]].x, l[p[0]].y)]});
		bsync({waitFor:[ X(l[p[1]].x, l[p[1]].y)]});
		bsync({request:[ O(l[p[2]].x, l[p[2]].y)]}, 40);
	}
});
