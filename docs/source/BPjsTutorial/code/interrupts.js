var CAKE_REQUEST = bp.Event("Cake Please");
var CAKE_READY   = bp.Event("Cake Served");

bp.registerBThread("Customer", function(){
  bsync({request:CAKE_REQUEST});
  bsync({waitFor:CAKE_READY});
});

// Burn the cake half of the times.
bp.registerBThread("Oven", function(){
  bsync({waitFor:bp.Event("Bake Start")});
  if ( bp.random.nextBoolean() ) {
    bsync({request:bp.Event("Cake Burnt"), block:bp.Event("Bake End")});
  } else {
    bsync({request:bp.Event("Cake Ready"), block:bp.Event("Bake End")});
  }
});

// Make me a cake as fast as you can
bp.registerBThread("Baker", function() {
  bsync({waitFor:CAKE_REQUEST});
  bsync({request:bp.Event("Buy Ingredients")});
  bsync({request:bp.Event("Mix Ingredients")});
  bsync({request:bp.Event("Bake Start")});
  bsync({waitFor:bp.Event("Cake Ready"),
       interrupt:bp.Event("Cake Burnt")});
  bsync({request:bp.Event("Decorate")});
  bsync({request:CAKE_READY});
});
