/* global bp */

bp.registerBThread(function () {
    setInterruptHandler( function(evt){
       bp.sync({interrupt:bp.Event("boom")}); // Blows up, can't call bp.sync here.
    });
    bp.sync({interrupt:bp.Event("boom")});
});

bp.registerBThread(function(){
   bp.sync({request:bp.Event("boom")});
});
