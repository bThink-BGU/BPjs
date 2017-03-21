var COUNT=4;
for ( var i=0; i<COUNT; i++ ) {
    bp.registerBThread("requestor-"+i, function(){
       bsync({request:bp.Event("e"+i)});
    });
};
