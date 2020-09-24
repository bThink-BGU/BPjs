var COUNT=4;
for ( var i=0; i<COUNT; i++ ) {
    (function(j){
        bp.registerBThread("requestor-" + j, function(){
           bp.sync({request:bp.Event("e"+j)});
        });
    })(i);
};
