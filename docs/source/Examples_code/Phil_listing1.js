function addPhil(philNum) {
	bp.registerBThread("Phil"+philNum, function() {
		while (true) {
			// Request to pick the right stick
			bsync({request: bp.Event("Pick"+philNum+"R")});
			// Request to pick the left stick
			bsync({request: bp.Event("Pick"+philNum+"L")});
			// Request to release the left stick
			bsync({request: bp.Event("Rel"+philNum+"L")});
			// Request to release the right stick
			bsync({request: bp.Event("Rel"+philNum+"R")});
		}
	});
};
