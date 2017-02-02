bp.registerBThread("bt-world",function(){
  bsync({waitFor:bp.Event("hello")});
  bsync({request:bp.Event("world")});
})

bp.registerBThread("bt-hi", function(){
  bsync({request:bp.Event("hello")});
})
