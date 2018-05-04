bp.registerBThread("AddThirdO", function() {
	while (true) {
		bsync({waitFor:[ O(l[p[0]].x, l[p[0]].y)]});
		bsync({waitFor:[ O(l[p[1]].x, l[p[1]].y)]});
		bsync({request:[ O(l[p[2]].x, l[p[2]].y)]}, 50);
	}
});
