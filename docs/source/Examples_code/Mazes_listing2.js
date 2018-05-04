bp.registerBThread("target", function () {
	bp.sync({waitFor: enterEvent(col, row)});

	bp.sync({request: TARGET_FOUND_EVENT,
		block: bp.allExcept(TARGET_FOUND_EVENT)});
});
