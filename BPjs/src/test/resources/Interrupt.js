/* global bp, noEvents, emptySet  */

/*
 * In this test, we expect two "breaking event"s, and no forbidden ones.
 */

var breakingEvent = bp.Event("breaking");
var forbiddenEvent = bp.Event("forbidden");

bp.registerBThread("requestor", function () {
  // request hotEvent twice.
    bsync( {request:breakingEvent} );
    bsync( {request:breakingEvent} );
});

bp.registerBThread("InterruptOnFirst", function () {
  bsync({interrupt:breakingEvent});
  bsync({request:forbiddenEvent});
});

bp.registerBThread("InterruptOnSecond", function () {
  bsync({waitFor:breakingEvent});
  bsync({interrupt:breakingEvent});
  bsync({request:forbiddenEvent} );
});
