bp.registerBThread("PreventFork00X", function() {
	while (true) {
		bsync({waitFor:[X(f[p[0]].x, f[p[0]].y)]});
		bsync({waitFor:[X(f[p[1]].x, f[p[1]].y)]});
		bsync({request:[O(0,0),O(0,2),O(2,0)]}, 30);
	}
});
