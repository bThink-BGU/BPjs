bp.registerBThread("hello world Patch", function(){
  bsync({waitFor:bp.Event("hello"), block:bp.Event("world")});
})
