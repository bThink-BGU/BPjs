/* global bp, block30 */



bp.registerBThread(function(){
  bp.sync({request: bp.Event("p1")}, 1);
});

bp.registerBThread(function(){
  bp.sync({request: bp.Event("p2")}, 2);
});

bp.registerBThread(function(){
  bp.sync({request: bp.Event("p3")}, 3);
});

bp.registerBThread(function(){
  bp.sync({request: bp.Event("p10")}, 10);
});

bp.registerBThread(function(){
  bp.sync({request: bp.Event("p20")}, 20);
});

bp.registerBThread(function(){
  bp.sync({request: bp.Event("p30")}, 30);
});

if ( block30 ) {
  bp.registerBThread(function(){
    bp.sync({block: bp.Event("p30")}, 30);
  });  
}