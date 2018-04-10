bp.registerBThread("hello world Patch", function(){
  bp.sync({waitFor:bp.Event("hello"), block:bp.Event("world")});
})
