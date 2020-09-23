bp.registerBThread("bt-world",function(){
  bp.sync({waitFor:bp.Event("hello")});
  bp.sync({request:bp.Event("world")});
})

bp.registerBThread("bt-hi", function(){
  bp.sync({request:bp.Event("hello")});
})
