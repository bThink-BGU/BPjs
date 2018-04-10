var CAKE_REQUEST = bp.Event("Cake Please");
var CAKE_READY   = bp.Event("Cake Served");

bp.registerBThread("Customer", function(){
  bp.sync({request:CAKE_REQUEST});
  bp.sync({waitFor:CAKE_READY});
});

// Burn the cake half of the times.
bp.registerBThread("Oven", function(){
  bp.sync({waitFor:bp.Event("Bake Start")});
  if ( bp.random.nextBoolean() ) {
    bp.sync({request:bp.Event("Cake Burnt"), block:bp.Event("Bake End")});
  } else {
    bp.sync({request:bp.Event("Cake Ready"), block:bp.Event("Bake End")});
  }
});

// Make me a cake as fast as you can
bp.registerBThread("Baker", function() {
  setInterruptHandler( function(evt) {
    bp.log.warn("Error making cake: " + evt);
    bp.enqueueExternalEvent(bp.Event("No cake for you!"));
    bp.enqueueExternalEvent(bp.Event("Come back - 1 month!"));
  });
  bp.sync({waitFor:CAKE_REQUEST});
  bp.sync({request:bp.Event("Buy Ingredients")});
  bp.sync({request:bp.Event("Mix Ingredients")});
  bp.sync({request:bp.Event("Bake Start")});
  bp.sync({waitFor:bp.Event("Cake Ready"),
       interrupt:bp.Event("Cake Burnt")});
  bp.sync({request:bp.Event("Decorate")});
  bp.sync({request:CAKE_READY});
});
