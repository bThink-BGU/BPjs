/* global bp */

// Simple bprogram with 4 possible orderings.
bp.log.info("Started program");
bp.registerBThread(function () {
    var a = 0;
    bsync({request: bp.Event("A1")});
    bp.log.info("a=" + a);
    a++;
    bsync({request: bp.Event("A2")});
    bp.log.info("a=" + a);
    a++;
    bsync({request: bp.Event("A3")});
    bp.log.info("a=" + a);
    a++;

});

bp.registerBThread(function () {
    var b = 0;
    bsync({request: bp.Event("B1")});
    bp.log.info("b="+b);
    b++;
    bsync({request: bp.Event("B2")});
    bp.log.info("b="+b);
    b++;
    bsync({request: bp.Event("B3")});
    bp.log.info("b="+b);
    b++;
});

bp.log.info("Main program done.");