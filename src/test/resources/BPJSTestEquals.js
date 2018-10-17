/* global bp */

bp.log.info('BPJSTestEquals');


bp.registerBThread("BThread 1", function() {
	while (true) {
		bp.sync({wait : bp.Event("X")});
		bp.sync({wait : bp.Event("X")});
	}
});
