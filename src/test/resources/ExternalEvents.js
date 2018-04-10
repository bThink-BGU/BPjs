/* global bp, noEvents, emptySet */

var in1a = bp.Event("in1a");
var in1b = bp.Event("in1b");
var ext1 = bp.Event("ext1");

bp.registerBThread("In1", function() {
    bp.sync( {request:in1a} );
    bp.sync( {waitFor:ext1} );
    bp.sync( {request:in1b} );
});
