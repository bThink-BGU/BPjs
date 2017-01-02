/* global bp */

/*
 * This BProgram uses the data field of events.
 */

var visibleEvents = bp.EventSet( "visible", function(e) {
    return e.data.eventType === "visible";
});
var hiddenEvents = bp.EventSet( "hidden", function(e) {
    return e.data.eventType === "hidden";
});

// create and request a visible event.
bp.registerBThread( "requestor", function(){
   bsync({request: bp.Event("e1", {eventType:"visible", nextEventName:"e2"})});
});

// This BThread waits for the first visible event, then
// requests an event based on the selected event.
bp.registerBThread( "waitForVisible", function(){
    var e = bsync({waitFor:visibleEvents});
    bsync({request:bp.Event(e.data.nextEventName, {eventType:"hidden"})});
});

// Waits for a hidden event, and requests an event based on it.
bp.registerBThread( "waitForHidden", function(){
    var ve = bsync({waitFor:visibleEvents});
    var he = bsync({waitFor:hiddenEvents});
    bsync({request:bp.Event(ve.name + he.name)});
});