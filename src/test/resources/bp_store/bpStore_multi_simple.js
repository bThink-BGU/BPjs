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

const WRITE = bp.Event("W");
const READ  = bp.Event("R");

bp.registerBThread("writer", function(){
    bp.store.put("v", 0);
    for ( let i=0; i<5; i++ ) {
        bp.store.put("v", bp.store.get("v")+1);
        bp.store.put("i", i);
        bp.sync({
            request: READ
        });
    }
});

bp.registerBThread("reader", function(){
    for ( let i=0; i<5; i++ ) {
        bp.sync({
            waitFor: READ
        });
        bp.log.info( bp.store.get("i") + "/" + bp.store.get("v"));
    }
});

bp.registerBThread("summary", function(){
    let i = 5;
    while ( i > 0  ) {
        bp.sync({waitFor:READ});
        i--;
    }
    bp.sync({request:bp.Event(bp.store.get("i") + "/" + bp.store.get("v"))});
});

