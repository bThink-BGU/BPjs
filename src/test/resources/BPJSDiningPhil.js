/* global bp, PHILOSOPHER_COUNT */
//PHILOSOPHER_COUNT = 9; // for convenience, this is now set up from the Java environment.

// for convenience, PHILOSOPHER_COUNT is now set up from the Java environment.
if (!PHILOSOPHER_COUNT) {
    PHILOSOPHER_COUNT = 5;
}

bp.log.info('Dinning philosophers with ' + PHILOSOPHER_COUNT + ' philosophers');

function addStick(i) {
    let j = (i % PHILOSOPHER_COUNT) + 1;

    bp.registerBThread("Stick" + i, function () {
        let pickMe = [bp.Event("Pick" + i + "R"), bp.Event("Pick" + j + "L")];
        let releaseMe = [bp.Event("Rel" + i + "R"), bp.Event("Rel" + j + "L")];

        while (true) {
            let e = bp.sync({waitFor: pickMe,
                             block: releaseMe});

            let wt = (e.name === "Pick" + i + "R") ? bp.Event("Rel" + i + "R") : bp.Event("Rel" + j + "L");
            bp.sync({waitFor: wt,
                     block: pickMe});
        }
    });
}

function addPhil(philNum) {
    let philname = String.fromCharCode(philNum+0x40);
    bp.registerBThread("Phil" + philname, function () {
        while (true) {
            // Request to pick the right stick
            bp.sync({
                request: bp.Event("Pick" + philNum + "R")
            });

            // Request to pick the left stick
            bp.sync({
                request: bp.Event("Pick" + philNum + "L")
            });

            // Request to release the left stick
            bp.sync({
                request: bp.Event("Rel" + philNum + "L")
            });

            // Request to release the right stick
            bp.sync({
                request: bp.Event("Rel" + philNum + "R")
            });
        }
    });
}
;

for (let i = 1; i <= PHILOSOPHER_COUNT; i++) {
    addStick(i);
    addPhil(i);
}
