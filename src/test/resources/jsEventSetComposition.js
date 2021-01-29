/* 
 * The MIT License
 *
 * Copyright 2021 michael.
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


/* global bp */

const eA = bp.Event("A");
const eB = bp.Event("B");
const eC = bp.Event("C");
const evtsA = eA.or(eB).or(eC);
const flowEvents = bp.eventSets.anyOf(eA, eB, eC);
const acEvents_and = bp.EventSet("*A*", evt=>evt.name.indexOf("A") > -1).and(
                      bp.EventSet("*C*", evt=>evt.name.indexOf("C") > -1));
const acEvents_allOf = bp.eventSets.allOf( bp.EventSet("*A*", evt=>evt.name.indexOf("A") > -1),
                                           bp.EventSet("*C*", evt=>evt.name.indexOf("C") > -1) );
const convolutedEA = bp.eventSets.not( eC.or(eB) ).and( evtsA );

bp.registerBThread("A", function(){
   bp.sync({request:eA});
   bp.sync({request:eB});
   bp.sync({request:eC});
});

bp.registerBThread("acCnt", function(){
    while ( true ) {
        bp.sync({waitFor:eA.or(eC)});
        bp.sync({request:bp.Event("AC"), block:flowEvents});
    }
});

bp.registerBThread("bcCnt", function(){
    while ( true ) {
        bp.sync({waitFor:eC.or(eB)});
        bp.sync({request:bp.Event("BC"), block:flowEvents});
    }
});

bp.registerBThread("acCnt", function(){
    while ( true ) {
        bp.sync({waitFor:acEvents_and});
        bp.sync({request:bp.Event("and-OK"), block:flowEvents});
    }
});

bp.registerBThread("acCnt", function(){
    while ( true ) {
        bp.sync({waitFor:acEvents_allOf});
        bp.sync({request:bp.Event("allOf-OK"), block:flowEvents});
    }
});

bp.registerBThread("eaCnt", function(){
    while ( true ) {
        bp.sync({waitFor:convolutedEA});
        bp.sync({request:bp.Event("conv-OK"), block:flowEvents});
    }
});