/* global bp */

bp.registerBThread("interrupted", function () {
    var testValue = "internalValue";
    setInterruptHandler( function(evt){
       bp.enqueueExternalEvent(evt); 
       bp.enqueueExternalEvent(bp.Event(testValue)); 
    });
    bp.sync({interrupt:bp.Event("boom")});
});

bp.registerBThread("request-and-wait",function(){
   bp.sync({request:bp.Event("boom")});
   bp.sync({waitFor:bp.all});
   bp.sync({waitFor:bp.all});
});
