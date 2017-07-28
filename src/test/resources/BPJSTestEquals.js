/* global bp, bsync */

bp.log.info('BPJSTestEquals')

bp.registerBThread("BThread 1", function() {
	var dudu = 1;
	bsync({ request : bp.Event("X") });
});
