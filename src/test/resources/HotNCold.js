/* global bp */

var coldEvent = bp.Event("coldEvent");
var hotEvent = bp.Event("hotEvent");

bp.registerBThread("HotBt", function() {
    bp.sync({request:hotEvent});
    bp.sync({request:hotEvent});
    bp.sync({request:hotEvent});
});

bp.registerBThread("ColdBt", function() {
    bp.sync({request:coldEvent});
    bp.sync({request:coldEvent});
    bp.sync({request:coldEvent});
});

bp.registerBThread("AlternatorBt", function() {
    for (var i = 0; i < 3; i++) {
        bp.sync({waitFor:coldEvent, block:hotEvent}); // block hot first, so as not to burn our thumb.
        bp.sync({waitFor:hotEvent, block:coldEvent});
    }
    bp.sync({request:bp.Event("allDone")});
});

bp.log.info("Setup done.");