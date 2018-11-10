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
/* global bp, COUNT */

// Simple program that counts to a given number, and returns the count.

var inc = bp.Event("Inc"); // "please increase counter"
var ack = bp.Event("Ack"); // "conter has been increased"
var end = bp.Event("End"); // "no more increments will happen"

bp.registerBThread( "initiator", function(){
    for ( var i=0; i<COUNT; i++ ) {
        bp.sync({request:inc});
    }
    bp.sync({request:end});
});

// No new increment can happen, before the previous increment 
// was ACK-ed.
bp.registerBThread("sillyBlocker", function(){
    while (true) {
        bp.sync({waitFor:inc});
        bp.sync({block:[inc, end], waitFor:ack});
    }
});

bp.registerBThread( "inc-er", function(){
    var counter = 0;
    var go = true;
    while ( go ) {
        var evt = bp.sync({waitFor:[inc, end]});
        if ( evt.name == inc.name ) {
            counter = counter+1;
        } else if ( evt.name == end.name ) {
            go = false;
        }
        bp.sync({request:ack});
    }
    
    bp.sync( {request:bp.Event("Result: " + counter)} );
});

