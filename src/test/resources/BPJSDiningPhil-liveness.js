/* global bp, PHILOSOPHER_COUNT */

/**
This file is a model of the famous dining philosophers problem, and has some
live requirements too. The BP model presented here has two types of b-threads:
1. philosophers: Decide when to eat by picking the sticks and then releasing them.
   There are no "eat" and "think" events, as these are implied.
2. sticks: Model real world requirements over chopsticks. E.g. a stick cannot be
   picked up if it's not down, or a stick can be held by at most a single philosopher
   at any given moment.

There are also two livness requirements in this model, ensuring that a picked-up
stick will eventually be put down, and that a hungry philosopher will eventually
eat.

The file is starts with definitions of the b-threads. It then builds the model
using the for-loop at the bottom.
*/

// If PHILOSOPHER_COUNT is not set up from the Java host program,
// it is set to a default value here.
if (!PHILOSOPHER_COUNT) {
  PHILOSOPHER_COUNT = 5;
}

bp.log.info('Dinning philosophers with ' + PHILOSOPHER_COUNT + ' philosophers');

function addStick(i) {
  var j = (i % PHILOSOPHER_COUNT) + 1;

  var pickMe = bp.EventSet("pick_" + i, function(e) {
    return e.name === "Pick" + i + "R" || e.name === "Pick" + j + "L";
  });
  var releaseMe = [bp.Event("Rel" + i + "R"), bp.Event("Rel" + j + "L")];

  // The stick b-thread
  bp.registerBThread("Stick" + i, function() {
    while (true) {
      var e = bp.sync({
        waitFor: pickMe,
        block: releaseMe
      });
      var wt = (e.name === "Pick" + i + "R") ? "Rel" + i + "R" : "Rel" + j + "L";
      bp.sync({
        waitFor: bp.Event(wt),
        block: releaseMe
      });
    }
  });

  // Liveness requirement:
  // If a stick was picked up, it will eventually be released.
  bp.registerBThread("[](pickup -> <>Released)", function() {
    while (true) {
      bp.sync({waitFor: pickMe});
      var reqRef = bp.setHot("Stick " + i + " was picked up but never released"); // <-- the string is optional
      bp.sync({waitFor: releaseMe})
      bp.setCold(refRef);
    }
  });

}

function addPhil(philNum) {
  bp.registerBThread("Phil" + philNum, function() {
    while (true) {
      bp.sync({request:bp.Event(i + "-is-hungry")})
      // Request to pick the right stick
      bp.sync({
        request: bp.Event("Pick" + philNum + "R")
      });

      // Request to pick the left stick
      bp.sync({
        request: bp.Event("Pick" + philNum + "L")
      });

      bp.sync({request:bp.Event(i + "-eats")})

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

  bp.registerBThread("NoStarvation", function() {
    while (true) {
      bp.sync({waitFor:bp.Event(i+"-is-hungry")});
      val reqRef = bp.setHot( i + " is hungry, but never got to eat.");
      bp.sync({waitFor:bp.Event(i+"-eats")});
      bp.setCold(reqRef);
    }
  });
};

// We're adding the philosophers here.
for (var i = 1; i <= PHILOSOPHER_COUNT; i++) {
  addStick(i);
  addPhil(i);
}
