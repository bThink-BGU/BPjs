/*  global bp */

// This script tests the JsEventSet class in the wild.
// Assuming that Javascript is wild. It is.

var firsts = bp.EventSet( "es1", function(e){
    return e.getName().startsWith("1st");
} );

var firstEvent = bp.Event("1stEvent");
var secondEvent = bp.Event("2ndEvent");

bp.registerBThread("first", function() {
    bp.sync({request:firstEvent});
});

/**
 * This Bthread will wait for all events that fall into
 * {@code eventSet}, and then will request the second event.
 */
bp.registerBThread("second", function() {
    bp.sync({waitFor:firsts});
    bp.sync({request:secondEvent});
});