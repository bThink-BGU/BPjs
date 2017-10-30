/* global bp, bsync, PHILOSOPHER_COUNT */
//PHILOSOPHER_COUNT = 9; // for convenience, this is now set up from the Java environment.

// for convenience, PHILOSOPHER_COUNT is now set up from the Java environment.
if ( ! PHILOSOPHER_COUNT ) {
    PHILOSOPHER_COUNT = 5;
} 

bp.log.info('Dinning philosophers with ' + PHILOSOPHER_COUNT + ' philosophers');

 function addStick(i) {
	var j = (i % PHILOSOPHER_COUNT) + 1;

	bp.registerBThread("Stick" + i, function() {

		while (true) {
			var e = bsync(
                    {
						waitFor : [ bp.Event("Pick" + i + "R"),
								    bp.Event("Pick" + j + "L") ],
						block : [ bp.Event("Rel" + i + "R"),
								  bp.Event("Rel" + j + "L") ]
					});

			var wt = (e.equals("Pick" + i + "R")) ? "Rel" + i + "R" : "Rel" + j	+ "L";
	
			bsync({
				waitFor : bp.Event(wt),
				block : [ bp.Event("Pick" + i + "R"),
						  bp.Event("Pick" + j + "L") ]
			});

			delete wt;
			delete e;
		}
	});
};




function addPhil(philNum) {
	bp.registerBThread("Phil" + philNum, function() {
		while (true) {
			// Request to pick the right stick
			bsync({
				request : bp.Event("Pick" + philNum + "R")
			});

			// Request to pick the left stick
			bsync({
				request : bp.Event("Pick" + philNum + "L")
			});

			// Request to release the left stick
			bsync({
				request : bp.Event("Rel" + philNum + "L")
			});

			// Request to release the right stick
			bsync({
				request : bp.Event("Rel" + philNum + "R")
			});
		}
	});
};

for (var i = 1; i <= PHILOSOPHER_COUNT; i++) {
	addStick(i);
	addPhil(i);
}
