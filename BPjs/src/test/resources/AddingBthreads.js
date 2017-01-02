/* global bp, noEvents, emptySet */
/* 
 * This little app adds bthreads dynamically.
 */

bp.log.info("Program Loaded");

// Define the events.
var kidADone   = bp.Event("kidADone");
var kidBDone   = bp.Event("kidBDone");
var parentDone = bp.Event("parentDone");

bp.registerBThread("parentBThread", function () {
    
    bp.log.info("parent started");
    
    // first one, text for behavior on the start() method.
    bp.registerBThread( function() {
        bp.log.info("kid a1 started");
        bsync({request:kidADone, block:parentDone});
    });
    bp.registerBThread( function() {
        bp.log.info("kid b1 started");
        bsync({request:kidBDone, block:parentDone});
    });
    bsync( {request: parentDone} );
    
    
    // second round, test for behavior on the resume() method.
    bp.registerBThread( function() {
        bsync({request:kidADone, block:parentDone});
    });
    bp.registerBThread( function() {
        bsync({request:kidBDone, block:parentDone});
    });
    bsync( {request: parentDone} );
    
});


