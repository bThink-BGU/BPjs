bp.registerBThread("bt-hi", function(){
  bsync({request:bp.Event("hello")});
})

bp.registerBThread("bt-world",function(){
  bsync({request:bp.Event("world")});
})
