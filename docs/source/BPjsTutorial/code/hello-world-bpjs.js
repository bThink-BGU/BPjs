bp.registerBThread("bpjs addition", function(){
  bp.sync({waitFor:bp.Event("hello")});
  bp.sync({request:bp.Event("BPjs"), block:bp.Event("world")});
})
