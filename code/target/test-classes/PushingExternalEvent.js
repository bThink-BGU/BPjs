/* global bp */

bp.registerBThread("HotBt", function () {
  // request hotEvent three times, in different verbosities.
    bsync({ request:bp.Event("start")} );
    bp.enqueueExternalEvent( bp.Event("external") );
    bsync({ waitFor: bp.Event("external") });
    bsync({ request:bp.Event("done")} );
});
