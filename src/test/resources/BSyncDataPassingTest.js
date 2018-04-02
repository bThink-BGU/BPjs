/* global bp, block30 */

function req( priority ) {
  bp.sync({request:bp.Event("p"+priority)}, priority);
}

bp.registerBThread(function(){
  req(1);
});

bp.registerBThread(function(){
  req(2);
});

bp.registerBThread(function(){
  req(3);
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