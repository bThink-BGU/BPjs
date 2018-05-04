bp.registerBThread("DetectOWin", function() {
	while (true) {
		bsync({waitFor:[O(l[p[0]].x, l[p[0]].y)]});
		bsync({waitFor:[O(l[p[1]].x, l[p[1]].y)]});
		bsync({waitFor:[O(l[p[2]].x, l[p[2]].y)]});
		bsync({request:[OWin]}, 100);
	}
});
