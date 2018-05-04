bp.registerBThread("DetectXWin", function() {
	while (true) {
		bp.sync({waitFor:[X(l[p[0]].x, l[p[0]].y)]});
		bp.sync({waitFor:[X(l[p[1]].x, l[p[1]].y)]});
		bp.sync({waitFor:[X(l[p[2]].x, l[p[2]].y)]});
		bp.sync({request:[XWin]}, 100);
	}
});
