/* global bp, java, importClass, Packages, EventSetsJsProxy */
importClass(Packages.il.ac.bgu.cs.bp.bpjs.execution.jsproxy.EventSetsJsProxy);
const PUTS = bp.EventSet("puts", function(e){ return e.name == "put";} );

bp.registerBThread("sender", function(){
    var arr = ["a","b","c"];
    for ( let idx=0; idx<arr.length; idx++ ) {
        var itm = arr[idx];
        var payload = {
            v: java.util.Optional.ofNullable(itm),
            notV: java.util.Optional.ofNullable(null)
        };
        bp.sync({
            request: bp.Event("put", payload)
        });
    }
});

bp.registerBThread("getter", function(){
    var set = new Set();
    let esProxy = EventSetsJsProxy.INSTANCE;
    let all = esProxy.all;
    
    while ( true ) {
        var e = bp.sync({waitFor:PUTS.and(all)});
        var letter = e.data.v.get();
        bp.log.info("added {0}", letter);
        set.add( letter );
    }
});
