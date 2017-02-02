bp.registerBThread("starter", function(){
  bsync({request:bp.Event("A", {type:"arrive"})});
  bsync({request:bp.Event("B", {type:"arrive"})});
  bsync({request:bp.Event("C", {type:"arrive"})});
});

bp.registerBThread("factory", function(){
  while (true) {
    var e = bsync({waitFor:bp.EventSet("arrivals",
                                       function(e){ return e.data && e.data.type=="arrive";}
                                     )
                  });
    var newBT = function() {
      bsync({request:bp.Event(this.e.name+"-a", {type:"archive"})});
    };
    newBT.e = e;
    bp.registerBThread(newBT);
  }
});
