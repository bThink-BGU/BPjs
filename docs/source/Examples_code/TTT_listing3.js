var move = bp.EventSet("Mvs", e=>e instanceof Move);

bp.registerBThread("DetectDraw", function() {
	for (var i=0; i<9; i++) {
		bp.sync({waitFor:[move]});
	}
	bp.sync({request:[draw]}, 90);
});
