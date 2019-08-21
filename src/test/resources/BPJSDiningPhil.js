/* global bp, PHILOSOPHER_COUNT */

// If PHILOSOPHER_COUNT is not set up from the Java host program, 
// it is set to a default value here.
if (!PHILOSOPHER_COUNT) 
    PHILOSOPHER_COUNT = 5;

bp.log.info('Dinning philosophers with ' + PHILOSOPHER_COUNT + ' philosophers');

function addStick(i) {
    var j = (i % PHILOSOPHER_COUNT) + 1;
    var pickMe = [bp.Event("Pick" + i + "R"), bp.Event("Pick" + j + "L")];
    var releaseMe = [bp.Event("Rel" + i + "R"), bp.Event("Rel" + j + "L")];
    
    bp.registerBThread("Stick" + i, function () {
        while (true) {
        	bp.sync({waitFor: pickMe, block: releaseMe});
            bp.sync({waitFor: releaseMe, block: pickMe});
        }
    });
}

function addPhil(philNum) {
    bp.registerBThread("Phil" + philNum, function () {
        while (true) {
            // Request to pick the right stick
            bp.sync({request: bp.Event("Pick" + philNum + "R")});
            // Request to pick the left stick
            bp.sync({request: bp.Event("Pick" + philNum + "L")});
            // Request to release the left stick
            bp.sync({request: bp.Event("Rel" + philNum + "L")});
            // Request to release the right stick
            bp.sync({request: bp.Event("Rel" + philNum + "R")});
        }
    });
};

for (var i = 1; i <= PHILOSOPHER_COUNT; i++) {
    addStick(i);
    addPhil(i);
}