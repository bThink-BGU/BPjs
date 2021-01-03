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


/* global bp, sw */

bp.log.info("sw=" + sw);

if ( sw == 1 ) {
    bp.registerBThread("non-event requestor", function(){
        bp.sync({request:bp.Event("NON_EVENT")});
        bp.sync({request:"NON_EVENT"});
        bp.sync({request:"NON_EVENT"});
        bp.sync({request:"NON_EVENT"});
    });
    bp.log.info("sw1 registered.");
}

if ( sw == 2 ) {
    bp.registerBThread(function(){
        bp.sync({request:[bp.Event("OK"),"NON_EVENT"]});
    });
}

if ( sw == 3 ) {
    bp.registerBThread(function(){
        bp.sync({waitFor:"NON_EVENT_or_SET"});
    });
}

if ( sw == 4 ) {
    bp.registerBThread(function(){
        bp.sync({block:"NON_EVENT_or_SET"});
    });
}

if ( sw == 5 ) {
    bp.registerBThread(function(){
        bp.sync({interrupt:"NON_EVENT_or_SET"});
    });
}

