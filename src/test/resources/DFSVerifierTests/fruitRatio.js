/* global bp */

////
// Refills 
////
var ADD_FRUIT = bp.EventSet("sADD_FRUIT", function(e){
 return e.name.equals("ADD_FRUIT");
});

/**
 * Creates a fruit addition event.
 * @param {Number} blueb amount of blueberries to add
 * @param {Number} raspb amount of raspberries to add
 */
function addFruits( blueb, raspb ) {
    return bp.Event("ADD_FRUIT", {
        blueberries:blueb,
        raspberries:raspb
    });
}

var ADD_BLUEB = addFruits(2,0);
var ADD_RASPB = addFruits(0,2);

bp.registerBThread( "BlueberryAdder", function(){
    var fruitIndex=0;
    while (true) {
        var evt = null;
        if ( fruitIndex < 0 ) {
            evt = bp.hot(true).sync({request:ADD_BLUEB,
                                     waitFor:ADD_FRUIT});
        } else {
            evt = bp.sync({waitFor:ADD_FRUIT});
        }
        fruitIndex = fruitIndex + 
                    evt.data.blueberries-evt.data.raspberries;
    }
});

bp.registerBThread( "RaspberryAdder", function(){
    var fruitIndex=0;
    while (true) {
        var evt = null;
        if ( fruitIndex > 0 ) {
            evt = bp.hot(true).sync({request:ADD_RASPB,
                                     waitFor:ADD_FRUIT});
        } else {
            evt = bp.sync({waitFor:ADD_FRUIT});
        }
        fruitIndex = fruitIndex + 
                    evt.data.blueberries-evt.data.raspberries;
    }
});

bp.registerBThread("script", function(){
    bp.sync({request:addFruits(0,1)});
});
