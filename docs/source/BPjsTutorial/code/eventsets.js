bp.registerBThread("orders", function(){
  var drinks=["black tea", "green tea", "espresso coffee", "flatwhite coffee",
              "latte coffee", "sparkling water"];
  // order 100 drinks
  for (var i = 0; i < 100; i++) {
    var idx = Math.floor(Math.random()*drinks.length);
    bp.sync({request:bp.Event(drinks[idx])});
  }
});

// Detect when it's time to grind more coffee
bp.registerBThread("coffee supply", function() {
  var coffeeOrderES = bp.EventSet( "coffee orders", function(evt){
    return evt.name.endsWith("coffee");
  });
  while ( true ) {
    for ( var i=0; i<10; i++ ) {
      bp.sync({waitFor:coffeeOrderES});
    }
    bp.sync({request:bp.Event("Grind more coffee!"),
           block:coffeeOrderES});
  }
});
