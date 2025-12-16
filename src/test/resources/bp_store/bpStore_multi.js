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

const START = bp.Event("START");
const DONE  = bp.Event("Done");

bp.log.setLevel("Fine");

bp.registerBThread("starter", function(){
    bp.store.put("v", {x:0, y:0});
    bp.sync({request:START});
    bp.sync({request:DONE});
    const v = bp.store.get("v");
    bp.sync({request: bp.Event("(" + v.x + "," + v.y + ")")});
});

bp.registerBThread("advanceX", function(){
   bp.sync({waitFor:START});
   for ( let i=0; i<5; i++ ) {
       bp.sync({request:bp.Event("advanceX"), block:DONE});
       const v = bp.store.get("v");
       v.x = v.x+1;
       bp.store.put("v", v);
       bp.log.info( v.x + "," + v.y );
   } 
});

bp.registerBThread("advanceY", function(){
   bp.sync({waitFor:START});
   for ( let i=0; i<5; i++ ) {
       bp.sync({request:bp.Event("advanceY"), block:DONE});
       const v = bp.store.get("v");
       v.y = v.y+1;
       bp.store.put("v", v);
       bp.log.info( v.x + "," + v.y );
   } 
});
