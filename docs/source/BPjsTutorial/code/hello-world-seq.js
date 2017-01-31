bp.registerBThread(function(){
  bsync({request:bp.Event("hello")});
  bsync({request:bp.Event("world")});
})
