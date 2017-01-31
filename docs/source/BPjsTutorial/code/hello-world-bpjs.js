bp.registerBThread("bpjs addition", function(){
  bsync({waitFor:bp.Event("hello")});
  bsync({request:bp.Event("BPjs"), block:bp.Event("world")});
})
