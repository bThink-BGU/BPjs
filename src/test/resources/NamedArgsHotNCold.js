/* global bp, noEvents, emptySet  */

var coldEvent = bp.Event("coldEvent");
var hotEvent = bp.Event("hotEvent");

bp.registerBThread("HotBt", function () {
  // request hotEvent three times, in different verbosities.
    bsync({ request:hotEvent,
            waitFor: [emptySet],
            block: emptySet} );
    bsync({ request: [hotEvent],
            waitFor: emptySet });
    bsync({ request: hotEvent });
});

bp.registerBThread("ColdBt", function () {
  //old-school, position-based bsyncs have to play along.
  bsync(coldEvent, emptySet, emptySet);
  bsync(coldEvent, emptySet, emptySet);
  bsync(coldEvent, emptySet, emptySet);
});

bp.registerBThread("AlternatorBt", function () {
   for (i = 0; i < 3; i++) {
       bsync({ waitFor: coldEvent, block: hotEvent} ); // block hot first, so as not to burn our thumb.
       bsync({ waitFor: hotEvent,  block: coldEvent} );
   }
   bsync({request:bp.Event("allDone")});
});
