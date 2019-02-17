/* 
 * The MIT License
 *
 * Copyright 2019 michael.
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


/*
 * A b-program that has multiple assertion errors, and we expect the verifier
 * to catch them all.
 */

/* global bp */

var EVT0 = bp.Event("0");
var EVT1 = bp.Event("1");
var EVT2 = bp.Event("2");
var EVT3 = bp.Event("3");
var EVT4 = bp.Event("4");

bp.registerBThread("pushFwd", function(){
    bp.sync({request:EVT0});
    bp.sync({request:[EVT1, EVT2, EVT3, EVT4]});
});

bp.registerBThread("boom1", function(){
    bp.sync({waitFor:EVT0});
    bp.sync({waitFor:EVT1});
    bp.hot(true).sync({waitFor:bp.Event("won't happen 1")});
});

bp.registerBThread("boom2", function(){
    bp.sync({waitFor:EVT0});
    bp.sync({waitFor:EVT2});
    bp.hot(true).sync({waitFor:bp.Event("won't happen 2")});
});

bp.registerBThread("boom3", function(){
    bp.sync({waitFor:EVT0});
    bp.sync({waitFor:EVT3});
    bp.hot(true).sync({waitFor:bp.Event("won't happen 3")});
});