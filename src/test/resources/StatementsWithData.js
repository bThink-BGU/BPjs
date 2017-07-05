/* global bp, noEvents, emptySet  */

// should select "1", "2", "3"

bp.registerBThread("bt-1", function () {
    bsync({ request: bp.Event("1") }, 1);
});

bp.registerBThread("bt-2", function () {
    bsync({ request: bp.Event("2") }, 2);
});

// Has no extra data on the bsync call.
bp.registerBThread("bt-3", function () {
    bsync( {request: bp.Event("3")} );
});
