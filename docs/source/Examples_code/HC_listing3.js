bp.registerBThread("control-temp", function() {
	while ( true ) {
		bp.sync({waitFor:COLD, block:HOT});
		bp.sync({waitFor:HOT, block:COLD});
	}
});
