/* global bp, noEvents, emptySet  */

var coldEvent = bp.Event("coldEvent");
var hotEvent = bp.Event("hotEvent");

bp.registerBThread("HotBt", function () {
  // request hotEvent three times, in different verbosities.
    bp.sync({ request:hotEvent,
            waitFor: [bp.none],
            block: bp.none} );
    bp.sync({ request: [hotEvent],
            waitFor: bp.none });
    bp.sync({ request: hotEvent });
});

bp.registerBThread("ColdBt", function () {
  bp.sync({request:coldEvent});
  bp.sync({request:[coldEvent]});
  bp.sync({request:coldEvent});
});

bp.registerBThread("AlternatorBt", function () {
   for (i = 0; i < 3; i++) {
       bp.sync({ waitFor: coldEvent, block: hotEvent} ); // block hot first, so as not to burn our thumb.
       bp.sync({ waitFor: hotEvent,  block: coldEvent} );
   }
   bp.sync({request:bp.Event("allDone")});
});
