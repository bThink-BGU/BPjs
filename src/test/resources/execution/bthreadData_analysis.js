/* 
 * The MIT License
 *
 * Copyright 2020 michael.
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

const plans = [ ["a","b","c","d"],
                ["1","2","3","4"] ];

function runPlan() {
    for ( let i in bp.thread.data ) {
        bp.sync({request:bp.Event(bp.thread.data[i])});
    }
}

function makeBT(plan) {
    bp.registerBThread("bt"+plan[0], plan, runPlan );
}

plans.forEach( p => makeBT(p) );

// For future reference: Below is the "pure"/"unrolled" version
// of the above program. It has 25 states (5*5). The nicer above
// program has about 182 states. Probably due to internal loop 
// counters etc.
// 
//bp.registerBThread("ad", function(){
//   bp.sync({request:bp.Event("a")}); 
//   bp.sync({request:bp.Event("b")}); 
//   bp.sync({request:bp.Event("c")}); 
//   bp.sync({request:bp.Event("d")}); 
//});
//bp.registerBThread("03", function(){
//   bp.sync({request:bp.Event("1")}); 
//   bp.sync({request:bp.Event("2")}); 
//   bp.sync({request:bp.Event("3")}); 
//   bp.sync({request:bp.Event("4")}); 
//});