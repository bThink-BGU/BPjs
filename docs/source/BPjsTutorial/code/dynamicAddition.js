bp.registerBThread("starter", function(){
  bp.sync({request:bp.Event("A", {type:"arrive"})});
  bp.sync({request:bp.Event("B", {type:"arrive"})});
  bp.sync({request:bp.Event("C", {type:"arrive"})});
});

bp.registerBThread("factory", function(){
  while (true) {
    var e = bp.sync({waitFor:bp.EventSet("arrivals",
                                       function(e){ return e.data && e.data.type=="arrive";}
                                     )
                  });
    var newBT = function() {
      bp.sync({request:bp.Event(this.e.name+"-a", {type:"archive"})});
    };
    newBT.e = e;
    bp.registerBThread(newBT);
  }
});
