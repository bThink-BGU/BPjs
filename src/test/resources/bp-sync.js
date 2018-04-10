
/* global bp */
/*
 * Demonstrating the new bp.sync usage.
 */
bp.registerBThread("helloer", function(){
  req("hello");
});

bp.registerBThread("worlder", function(){
  waitFor("hello");
  req("world");
});

function waitFor(str) {
  bp.sync({waitFor:bp.Event(str)});
}

function req(str){
  bp.sync({request:bp.Event(str)});
}
