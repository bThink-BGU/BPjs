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
        var pickMe = bp.EventSet("pick_"+i, function(e){
          return e.name === "Pick"+i+"R" || e.name === "Pick"+j+"L";
        });
        var releaseMe = [bp.Event("Rel"+i+"R"), bp.Event("Rel"+j+"L")];

        while (true) {
			var e = bsync({ waitFor: pickMe,
                        	  block: releaseMe });

			var wt = (e.name==="Pick"+i+"R") ? "Rel"+i+"R" : "Rel"+j+"L";
			bsync({ waitFor: bp.Event(wt),
				      block: releaseMe });
			
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
