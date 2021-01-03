bp.registerBThread("BTwDT", {some:"Data", num:17}, function(){
    bp.log.info("BT data: {0}", bp.thread.data);
    bp.sync({request:bp.Event(bp.thread.data.some)});
});