/* global bp */
/*
 * Demonstrating the new bp.sync usage.
 */

var eventName = "sampleEvent";
var sampleEvent = bp.Event(eventName);

var anEventSet = bp.EventSet( "visible", function(e) {
    return e.name == eventName ;
});

bp.registerBThread("helloer", function () {
    bp.sync({request: sampleEvent});
    bp.sync({request: sampleEvent, block: anEventSet});
});

