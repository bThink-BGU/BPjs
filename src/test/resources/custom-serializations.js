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
    while ( true ) {
        var e = bp.sync({waitFor:PUTS});
        var letter = e.data.v.get();
        bp.log.info("added {0}", letter);
        set.add( letter );
    }
});
