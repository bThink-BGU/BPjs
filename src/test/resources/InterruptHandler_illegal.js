/* global bp */

bp.registerBThread(function () {
    bp.setInterruptHandler( function(evt){
       bp.sync({request:bp.Event("boom")}); // Blows up, can't call bp.sync here.
    });
    bp.sync({interrupt:bp.Event("boom")});
});

bp.registerBThread(function(){
   bp.sync({request:bp.Event("boom")});
});
