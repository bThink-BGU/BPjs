bp.registerBThread("control-temp", function() {
	while ( true ) {
		bsync({waitFor:COLD, block:HOT});
		bsync({waitFor:HOT, block:COLD});
	}
});
