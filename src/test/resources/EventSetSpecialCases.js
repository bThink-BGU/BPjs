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

const e1 = bp.Event("e1");
const e2 = bp.Event("e2");
const es1 = bp.EventSet("es1", function(a){ return a==e1; } );
const es2 = bp.EventSet("es2", function(a){ return a==e2; } );

bp.registerBThread("es12", function(){    
    bp.sync({waitFor:[es1,es2]});
});

bp.registerBThread("es21", function(){    
    bp.sync({waitFor:[es2,es1]});
});

bp.registerBThread("n1", function(){
    bp.sync({waitFor:[]});
});

bp.registerBThread("n2", function(){
    bp.sync({waitFor:[bp.none]});
});

bp.registerBThread("n3", function(){
    bp.sync({waitFor:bp.none});
});

bp.registerBThread("driver", function(){
    bp.sync({request:bp.Event("XXX")});
    bp.ASSERT(false, ""); // crash so that we get a trace we can inspect.
});
