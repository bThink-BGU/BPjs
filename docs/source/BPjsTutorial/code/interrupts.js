var CAKE_REQUEST = bp.Event("Cake");

bp.registerBThread("cake request", function(){
  bsync({request:CAKE_REQUEST});
});

// Burn the cake half of the times.
bp.registerBThread("Bad Luck", function(){
  bsync({waitFor:bp.Event("Bake Start")});
  if ( Math.random() > 0.5 ) {
    bsync({request:bp.Event("Cake Burnt"), block:bp.Event("Bake End")});
  }
});

// Make me a cake as fast as you can
bp.registerBThread("baker", function() {
  bsync({waitFor:CAKE_REQUEST});
  bsync({request:bp.Event("Buy Ingredients")});
  bsync({request:bp.Event("Mix Ingredients")});
  bsync({request:bp.Event("Bake Start")});
  bsync({request:bp.Event("Bake End"), interrupt:bp.Event("Cake Burnt")});
  bsync({request:bp.Event("Decorate")});
  bsync({request:bp.Event("Serve")});
});
