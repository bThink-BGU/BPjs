/* global bp, bsync */
N = 5;

bp.log.info('Dinning philosophers with ' + N + ' philosophers')

addStick = function(i) {
	var j = (i % N) + 1;

	bp.registerBThread("Stick " + i, function() {
		
		var e = "";
		var wt = "";
		
		while (true) {
			e = bsync({
						waitFor : [ bp.Event("Pick" + i + "R"),
								    bp.Event("Pick" + j + "L") ],

						block : [ bp.Event("Rel" + i + "R"),
			  					  bp.Event("Rel" + j + "L") ],
			  					  
					}, "1 " + wt + e).name;
			
			
			wt = (e.equals("Pick" + i + "R")) ? "Rel" + i + "R" : "Rel" + j + "L";

			bsync({
				waitFor : bp.Event(wt),

				block : [ bp.Event("Pick" + i + "R"),
						  bp.Event("Pick" + j + "L") ]	
			
			}, "2 " + wt + e);

			wt = "";
			e = "";
		}
	});
};

addPhil = function(i) {
	bp.registerBThread("Phil" + i, function() {
		while (true) {
			// Request to pick the right stick
			bsync({
				request : bp.Event("Pick" + i + "R")
			},"1");

			// Request to pick the left stick
			bsync({
				request : bp.Event("Pick" + i + "L")
			},"2");

			// Request to release the left stick
			bsync({
				request : bp.Event("Rel" + i + "L")
			}, "3");

			// Request to release the right stick
			bsync({
				request : bp.Event("Rel" + i + "R")
			}, "4");
		}
	});
};

for (i = 1; i <= N; i++) {
	addStick(i);
	addPhil(i);
}
