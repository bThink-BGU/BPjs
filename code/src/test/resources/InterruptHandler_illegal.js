/* global bp */

bp.registerBThread(function () {
    setInterruptHandler( function(evt){
       bsync({interrupt:bp.Event("boom")}); // Blows up, can't call bsync here.
    });
    bsync({interrupt:bp.Event("boom")});
});

bp.registerBThread(function(){
   bsync({request:bp.Event("boom")});
});
