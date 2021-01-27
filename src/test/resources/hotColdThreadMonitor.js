/* global bp */
var coldEvent = bp.Event("coldEvent");
var hotEvent = bp.Event("hotEvent");

bp.registerBThread("HotBt", function() {
    bp.sync({request:hotEvent});
    bp.sync({request:hotEvent});
});

bp.registerBThread("ColdBt", function() {
    bp.sync({request:coldEvent});
    bp.sync({request:coldEvent});
});

bp.registerBThread("counter", function(){
    bp.thread.data = {h:0,c:0};
    let e=0;
    while (true) {
        e = bp.sync({waitFor:bp.all}).name.substring(0,1);
        bp.thread.data[e] = bp.thread.data[e]+1;
        e = 0;
//        bp.log.info(bp.thread.data);
    }
});
