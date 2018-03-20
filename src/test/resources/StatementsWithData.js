/* global bp, noEvents, emptySet  */

// should select "1", "2", "3"

bp.registerBThread("bt-1", function() {
    bp.sync({ request: bp.Event("1") }, 100);
});

bp.registerBThread("bt-2", function() {
    bp.sync({ request: bp.Event("2") }, 10);
});

// Has no extra data on the bp.sync call.‰
bp.registerBThread("bt-3", function() {
    bp.sync( {request: bp.Event("3")} );
});


