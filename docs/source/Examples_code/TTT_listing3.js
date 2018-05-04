var move = bp.EventSet("Mvs", e=>e instanceof Move);

bp.registerBThread("DetectDraw", function() {
	for (var i=0; i<9; i++) {
		bsync({waitFor:[move]});
	}
	bsync({request:[draw]}, 90);
});
