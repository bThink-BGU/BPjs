bp.registerBThread("Clck("+r+","+c+")", function() {
	while (true) {
		bsync({waitFor:[Click(r,c)]});
		bsync({request:[X(r,c)]});
	}
});
