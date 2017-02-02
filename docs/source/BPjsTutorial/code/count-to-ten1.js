bp.registerBThread("starter", function(){
  bsync({request:bp.Event("Just an Event")});
  bsync({request:bp.Event("withData", {type:"counter",value:0})});
  bsync({request:bp.Event("Just an Event")});
});

// Increasing the counter
bp.registerBThread("Increaser", function(){
  var counterEvents = bp.EventSet("counters", function(evt) {
    return (evt.data==null) ?
              false :
              evt.data.type && evt.data.type=="counter";
  });
  var counterEvt = bsync({waitFor:counterEvents});
  while (true) {
    bp.log.info( counterEvt.name + ": " + counterEvt.data.value );
    var next = bp.Event(counterEvt.name, {type:counterEvt.data.type,
                                         value:counterEvt.data.value+1});
    counterEvt = bsync({request:next});
  }
});

// Capping the counters at 10.
bp.registerBThread("Capper", function() {
  var countersAt10 = bp.EventSet("done", function(evt) {
    return (evt.data==null) ?
            false :
            evt.data.type=="counter" && evt.data.value==10;
  });
  bsync({block:countersAt10});
});
