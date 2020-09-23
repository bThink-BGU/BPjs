bp.registerBThread(function(){
  bp.sync({request:bp.Event("hello")});
  bp.sync({request:bp.Event("world")});
})
