bp.registerBThread("starter", function(){
  bp.sync({request:bp.Event("Just an Event")});
  bp.sync({request:bp.Event("withData", {type:"counter",value:0})});
  bp.sync({request:bp.Event("Just an Event")});
});

// Increasing the counter
bp.registerBThread("Increaser", function(){
  var counterEvents = bp.EventSet("counters", function(evt) {
    return (evt.data==null) ?
              false :
              evt.data.type && evt.data.type=="counter";
  });
  var counterEvt = bp.sync({waitFor:counterEvents});
  while (true) {
    bp.log.info( counterEvt.name + ": " + counterEvt.data.value );
    var next = bp.Event(counterEvt.name, {type:counterEvt.data.type,
                                         value:counterEvt.data.value+1});
    counterEvt = bp.sync({request:next});
  }
});

// Capping the counters at 10.
bp.registerBThread("Capper", function() {
  var countersAt10 = bp.EventSet("done", function(evt) {
    return (evt.data==null) ?
            false :
            evt.data.type=="counter" && evt.data.value==10;
  });
  bp.sync({block:countersAt10});
});
