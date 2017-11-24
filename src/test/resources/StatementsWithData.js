/* global bp, noEvents, emptySet  */

// should select "1", "2", "3"

bp.registerBThread("bt-1", function() {
    bsync({ request: bp.Event("1") }, 100);
});

bp.registerBThread("bt-2", function() {
    bsync({ request: bp.Event("2") }, 10);
});

// Has no extra data on the bsync call.‰
bp.registerBThread("bt-3", function() {
    bsync( {request: bp.Event("3")} );
});


