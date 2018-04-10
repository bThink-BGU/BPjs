/* global bp */
//bp.setDaemonMode( true );
bp.registerBThread( function () {
  // request hotEvent three times, in different verbosities.
  bp.sync({ request:bp.Event("start")} );
  bp.enqueueExternalEvent( bp.Event("external") );
  bp.sync({ waitFor: bp.Event("external") });
  bp.sync({ request:bp.Event("done")} );
});
