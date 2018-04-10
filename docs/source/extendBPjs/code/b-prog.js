bp.registerBThread("bt-1", function () {
    bp.sync({ request: bp.Event("1") }, 1);
});

bp.registerBThread("bt-2", function () {
    bp.sync({ request: bp.Event("2") }, 2);
});

// Has no extra data on the bp.sync call.
bp.registerBThread("bt-3", function () {
    bp.sync( {request: bp.Event("3")} );
});
