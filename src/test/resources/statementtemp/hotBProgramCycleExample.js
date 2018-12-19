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


/* global bp */

/*
 * The general state-space for the stuck b-thread is this (H) - hot
 * 
 * <code>
 *    a    b    c    d 
 * ( )->(H)->(P)->(H)->( )-X
 *       |         |e
 *       +--<--<---+ 
 * </code>
 * 
 * P: The "main" b-thread is not hot, but another b-thread is.
 *       
 */

var a = bp.Event("a");
var b = bp.Event("b");
var c = bp.Event("c");
var d = bp.Event("d");
var e = bp.Event("e");

bp.registerBThread("main", function(){
    bp.sync({request:a});
    var go = true;
    while ( go ) {
        bp.hot(true).sync({request:b});
        bp.sync({request:c});
        var evt=bp.hot(true).sync({waitFor:[d,e]});
        if ( evt.name == "d" ) {
            go=false;
        }
    }
});

bp.registerBThread("b-requires-c", function(){
    while ( true ) {
        bp.sync({waitFor:b});
        bp.hot(true).sync({waitFor:c});
    }
});

bp.registerBThread("requesting-d", function(){
    while ( true ) {
        bp.sync({waitFor:c});
        bp.sync({request:d, waitFor:e});
    }
});

bp.registerBThread("requesting-e", function(){
    while ( true ) {
        bp.sync({waitFor:c});
        bp.sync({request:e, waitFor:d});
    }
});