bp.registerBThread("DetectOWin", function() {
	while (true) {
		bp.sync({waitFor:[O(l[p[0]].x, l[p[0]].y)]});
		bp.sync({waitFor:[O(l[p[1]].x, l[p[1]].y)]});
		bp.sync({waitFor:[O(l[p[2]].x, l[p[2]].y)]});
		bp.sync({request:[OWin]}, 100);
	}
});
