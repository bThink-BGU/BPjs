bp.registerBThread("DetectXWin", function() {
	while (true) {
		bsync({waitFor:[X(l[p[0]].x, l[p[0]].y)]});
		bsync({waitFor:[X(l[p[1]].x, l[p[1]].y)]});
		bsync({waitFor:[X(l[p[2]].x, l[p[2]].y)]});
		bsync({request:[XWin]}, 100);
	}
});
