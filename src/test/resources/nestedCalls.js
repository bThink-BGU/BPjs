/* global bp, bsync
 * The MIT License
 *
 * Copyright 2017 michael.
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


/**
 * Set of possible programs that can interact with each other
 * Taskset A: A bthread that calls a three different functions,
 *      in each, trivial calculation then call to Bsync statement
 *      each function in turn requests A,B,C
 * Taskset B: A bthread that runs as a standalone recursive function,
 *            each time it recurses (up to limit) it requests A,B,C
 * Taskset C: (TODO) A bthread that constructs a nested object for each bsync statement
 *                  Constructing a nested object of A,B,C statements
 * There's a sentinel BThread running A,B,C forever.
 */


// This JS file can create all the results of verification session.

ITERATION_COUNT = 10;

// This b-thread goes forward until it's done.
bp.registerBThread("forward", function () {
    for (var forward = 0; forward < ITERATION_COUNT; forward++) {
        bp.sync({request: bp.Event("A")});
        bp.sync({request: bp.Event("B")});
        bp.sync({request: bp.Event("C")});
        bp.log.info("master loop iteration - " + forward)
    }
});

//utility functions
function summerA(limit) {
    var result = 0;
    if (limit != 0) {
        result = limit + summerA(limit - 1);
    }
    bp.sync({waitFor: bp.Event("A")});
    return result;
}

function factB(base, limit) {
    var result = base;
    if (limit <= 0) {
        result = result * factB(limit - 1);
    }
    bp.sync({waitFor: bp.Event("B")});
    return result;
}

function evenC(number) {
    var flag = false;
    if ((number % 2) == 0) {
        flag = true;
    }
    bp.sync({waitFor: bp.Event("C")});
    return flag;
}

var REQUESTOR = [bp.Event("A"), bp.Event("B"), bp.Event("C")];

function RecursiveRequested(depth) {
    if (depth == 3) {
        return;
    } else {
        bp.sync({waitFor: REQUESTOR[depth]});
        RecursiveRequested(depth + 1);
    }
}

// This b-thread will wait forever, so we can verify that the verifier does not
//   consider waiting as part of a deadlock.
if (taskA) {
    bp.registerBThread("calcBthread", function () {
        for (var i = 0; i < ITERATION_COUNT; i++) {
            bp.log.info("calc Loop " + i);
            var result = summerA(i);
            bp.log.info(result);
            result = factB(i);
            bp.log.info(result);
            result = evenC(i);
            bp.log.info(result);
        }
    });
}

function RecursiveObject(depth,mapObj,infinite) {
    if (infinite == ITERATION_COUNT) {
        return mapObj;
    }
    if (depth == 3) {
        depth = 0;
    }
    mapObj = {'event':REQUESTOR[depth],'nest':mapObj,'depth':depth};
    bp.log.info(mapObj);
    bp.sync({waitFor:mapObj['event']});
    RecursiveObject(depth+1,mapObj,infinite+1);
}

if (taskB) {
    bp.registerBThread("calcBthread", function () {
        for (var j = 0; j < ITERATION_COUNT; j++) {
            bp.log.info("recursive loop " + j);
            RecursiveRequested(0);
        }
    });
}

if (taskC) {
    bp.registerBThread("calcCthread", function () {
        RecursiveObject(0,{},0);
    });
}
