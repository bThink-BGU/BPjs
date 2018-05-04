bp.registerBThread("Clck("+r+","+c+")", function() {
	while (true) {
		bp.sync({waitFor:[Click(r,c)]});
		bp.sync({request:[X(r,c)]});
	}
});
