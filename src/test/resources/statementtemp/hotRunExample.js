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

/* 
 
 ->( )-a->(A)-b->(B)-c->(C)-+
           ^                |
           +-------a--------+
 
 bthreads:
  A=>B: hot at state A // "A is always followed by B", [](A -> O(<>B)
  B=>C: hot at state B
  C=>A: hot at state C
  cold: always cool, progresses this show
 */

const a = bp.Event("a");
const b = bp.Event("b");
const c = bp.Event("c");

bp.registerBThread("cold", function(){
    while ( true ) {
        bp.sync({request:a});
        bp.sync({request:b});
        bp.sync({request:c});
    }
});

bp.registerBThread("A=>B", function(){
    while ( true ) {
        bp.sync({waitFor:a});
        bp.hot(true).sync({waitFor:b});
    }
});

bp.registerBThread("B=>C", function(){
    while ( true ) {
        bp.sync({waitFor:b});
        bp.hot(true).sync({waitFor:c});
    }
});

bp.registerBThread("C=>A", function(){
    while ( true ) {
        bp.sync({waitFor:c});
        bp.hot(true).sync({waitFor:a});
    }
});
