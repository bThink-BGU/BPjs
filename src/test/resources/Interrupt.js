/* global bp, noEvents, emptySet  */

/*
 * In this test, we expect two "breaking event"s, and no forbidden ones.
 */

var breakingEvent = bp.Event("breaking");
var forbiddenEvent = bp.Event("forbidden");

bp.registerBThread("requestor", function () {
  // request hotEvent twice.
    bp.sync( {request:breakingEvent} );
    bp.sync( {request:breakingEvent} );
});

bp.registerBThread("InterruptOnFirst", function () {
  bp.sync({interrupt:breakingEvent});
  bp.sync({request:forbiddenEvent});
});

bp.registerBThread("InterruptOnSecond", function () {
  bp.sync({waitFor:breakingEvent});
  bp.sync({interrupt:breakingEvent});
  bp.sync({request:forbiddenEvent} );
});
