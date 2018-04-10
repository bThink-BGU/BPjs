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
    bp.registerBThread( "kid a1", function() {
        bp.log.info("kid a1 started");
        bp.sync({request:kidADone, block:parentDone});
    });
    bp.registerBThread( "kid b1", function() {
        bp.log.info("kid b1 started");
        bp.sync({request:kidBDone, block:parentDone});
    });
    bp.sync( {request: parentDone} );
    
    
    // second round, test for behavior on the resume() method.
    bp.registerBThread( "kid a2", function() {
        bp.sync({request:kidADone, block:parentDone});
    });
    bp.registerBThread( "kid b2", function() {
        bp.sync({request:kidBDone, block:parentDone});
    });
    bp.sync( {request: parentDone} );
    
});


