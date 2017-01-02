/* global bp, emptySet, noEvents */

var coldEvent = bp.Event("coldEvent");
var hotEvent = bp.Event("hotEvent");

bp.registerBThread("HotBt", function() {
    bsync({request:hotEvent});
    bsync({request:hotEvent});
    bsync({request:hotEvent});
});

bp.registerBThread("ColdBt", function() {
    bsync({request:coldEvent});
    bsync({request:coldEvent});
    bsync({request:coldEvent});
});

bp.registerBThread("AlternatorBt", function() {
    for (i = 0; i < 3; i++) {
        bsync({waitFor:coldEvent, block:hotEvent}); // block hot first, so as not to burn our thumb.
        bsync({waitFor:hotEvent, block:coldEvent});
    }
    bsync({request:bp.Event("allDone")});
});
