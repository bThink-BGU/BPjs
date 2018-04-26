/* 
 * The MIT License
 *
 * Copyright 2018 michael.
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


/* global bp, addWaiter, createDeadlock, createFailedAssertion */

// This JS file can create all the results of verification session.

// This b-thread goes forward until it's done.
bp.registerBThread("forward", function(){
    bp.sync({request:bp.Event("A")});
    bp.sync({request:bp.Event("B")});
    bp.sync({request:bp.Event("C")});
});

// This b-thread will wait forever, so we can verify that the verifier does not
//   consider waiting as part of a deadlock.
if ( addWaiter ) {
    bp.registerBThread("waitingForever", function() {
        bp.sync({waitFor:bp.Event("Z")});
    });
}
if ( createDeadlock ) {
    bp.registerBThread("deadlocker", function() {
       bp.sync({block:bp.Event("B")}) ;
    });
}

if ( createFailedAssertion ) {
    bp.registerBThread("assertor", function(){
       bp.sync({waitFor:bp.Event("B")});
       bp.ASSERT( false, "B happened" );
    });
}

