/* global bp */

bp.registerBThread("interrupted", function () {
    var testValue = "internalValue";
    setInterruptHandler( function(evt){
       bp.enqueueExternalEvent(evt); 
       bp.enqueueExternalEvent(bp.Event(testValue)); 
    });
    bsync({interrupt:bp.Event("boom")});
});

bp.registerBThread("request-and-wait",function(){
   bsync({request:bp.Event("boom")});
   bsync({waitFor:bp.all});
   bsync({waitFor:bp.all});
});
