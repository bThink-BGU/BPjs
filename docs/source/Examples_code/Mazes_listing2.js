bp.registerBThread("target", function () {
	bsync({waitFor: enterEvent(col, row)});

	bsync({request: TARGET_FOUND_EVENT,
		block: bp.allExcept(TARGET_FOUND_EVENT)});
});
