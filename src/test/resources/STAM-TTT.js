/* global bp */

// We expected to see 4 visited states, but only three are reported.

bp.registerBThread("STAM1", function() {
	while (true) {
		bp.sync({ request:[ bp.Event("STAM1") ] });
	}
});

bp.registerBThread("STAM2", function() {
	while (true) {
		bp.sync({ request:[ bp.Event("STAM2") ] });
	}
});
