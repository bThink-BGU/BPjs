/* global bp, noEvents, emptySet */

// Waits for three external events, handle each one. Then quits.

bp.setDaemonMode( true );

var in1a = bp.Event("in1a");
var in1b = bp.Event("in1b");
var ext1 = bp.Event("ext1");

bp.registerBThread("handler", function() {
    for ( var i=0; i<3; i++ ){
        bsync( {waitFor:ext1} );
        bsync( {request:in1a} );
        bsync( {request:in1b} );
    }
    bp.setDaemonMode( false );
});
