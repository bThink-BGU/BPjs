bp.registerBThread("PreventDiagForkX", function() {
	while (true) {
		bsync({waitFor:[X(f[p[0]].x, f[p[0]].y)]});
		bsync({waitFor:[X(f[p[1]].x, f[p[1]].y)]});
		bsync({request:[O(0,1),O(1,0),O(1,2),O(2,1)]},28);
	}
});
