bp.log.info("registering bthreads - start");
var LOG_LEVELS = ["Fine", "Info", "Warn", "Off"];

bp.registerBThread( "event generator", function(){
  for (var i = 0; i < LOG_LEVELS.length; i++) {
    bp.sync({request:bp.Event("event " + String(i))});
  }
});

bp.registerBThread( "event logging", function() {
  var idx = 0;
  while (true) {
    var evt = bp.sync({waitFor:bp.all});
    bp.log.setLevel(LOG_LEVELS[idx]);
    bp.log.warn( evt.name );
    bp.log.info( evt.name );
    bp.log.fine( evt.name );
    idx++;
  }
})

bp.log.info("registering bthreads - done");
