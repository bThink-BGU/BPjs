bp.registerBThread("bt-hi", function(){
  bp.sync({request:bp.Event("hello")});
})

bp.registerBThread("bt-world",function(){
  bp.sync({request:bp.Event("world")});
})
