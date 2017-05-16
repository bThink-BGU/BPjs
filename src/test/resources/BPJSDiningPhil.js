/* global bp, bsync */
N = 4;

addPhil = function(i) {
	bp.registerBThread("Phil" + i, function() {
		while (true) {
			// Request to pick the right stick
			bsync({ request : bp.Event("Pick" + i + "R") });

			// Request to pick the left stick
			bsync({ request : bp.Event("Pick" + i + "L") });

			// Request to release the left stick
			bsync({ request : bp.Event("Rel" + i + "L") });

			// Request to release the right stick
			bsync({ request : bp.Event("Rel" + i + "R") });
		}
	});
};

addStick = function(i) {
	var j = (i % N) + 1;

	bp.registerBThread("Stick " + i,
			function() {
				while (true) {
					var e = bsync({
						waitFor : [ bp.Event("Pick" + i + "R"),
								    bp.Event("Pick" + j + "L") ],
						block : [ bp.Event("Rel" + i + "R"),
								  bp.Event("Rel" + j + "L") ] }).name;

					var wt = (e.equals("Pick" + i + "R")) ? "Rel" + i + "R"
							: "Rel" + j + "L";
					bsync({
						waitFor : bp.Event(wt),
						block : [ bp.Event("Pick" + i + "R"),
								  bp.Event("Pick" + j + "L") ] });
				}
			});
};

for (i = 1; i <= N; i++) {
	addPhil(i);
	addStick(i);
}
