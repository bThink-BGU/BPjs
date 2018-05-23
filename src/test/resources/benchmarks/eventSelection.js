/*
 * The MIT License
 *
 * Copyright 2018 acepace.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


/* globals
    INITIAL_ARRAY_SIZE - Size of initial array of events
    ARRAY_STEP - Number of events to add per step
    NUM_STEPS - Number of steps to take
*/

bp.log.setLevel("Info");
function pusher(i, j) {
    let toPush = bp.Event("event"+i); //TODO: use string formatting if Rhino allows
    return toPush;
}


var GLOBAL_ARRAY = [];
for (let init = 0; init < INITIAL_ARRAY_SIZE; init++) {
    GLOBAL_ARRAY.push(pusher(-1,-1)); //dummy data at init
}

bp.registerBThread("eventPicker", function () {
    for (let i = 0; i < NUM_STEPS; i++) {
        for (let j = 0; j < ARRAY_STEP; j++) {
            GLOBAL_ARRAY.push(pusher(i, j));
        }
        let e = bp.sync({request:GLOBAL_ARRAY});
        bp.log.info("at step "+ i +" the event is " +e.name);
    }
});