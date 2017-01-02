/* global bp */


var anEventSet = bp.EventSet( "visible", function(e) {
    return e.data !== null ? e.data.eventType === "visible" : false;
});

bp.registerBThread( "requestor1", function(){
   bsync({request: bp.Event("e11")});
});
bp.registerBThread( "requestor2", function(){
   bsync({request: bp.Event("e21")});
});

bp.registerBThread( "blocker", function(){
    bsync({
        waitFor: [ bp.Event("e11"), bp.Event("e12"), anEventSet],
        block: [bp.Event("e21"), bp.Event("e22"), anEventSet ]
    });
});
