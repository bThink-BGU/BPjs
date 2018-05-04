function addPhil(philNum) {
	bp.registerBThread("Phil"+philNum, function() {
		while (true) {
			// Request to pick the right stick
			bp.sync({request: bp.Event("Pick"+philNum+"R")});
			// Request to pick the left stick
			bp.sync({request: bp.Event("Pick"+philNum+"L")});
			// Request to release the left stick
			bp.sync({request: bp.Event("Rel"+philNum+"L")});
			// Request to release the right stick
			bp.sync({request: bp.Event("Rel"+philNum+"R")});
		}
	});
};
