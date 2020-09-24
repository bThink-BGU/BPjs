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

let dispatch;// = "writeNew";
if ( ! dispatch ) {
    dispatch = "readFromExisting";
}

function req(evtName) {
    bp.sync({request:bp.Event(evtName)});
}

if ( dispatch.indexOf("readFromExisting") > -1 ) {
    bp.registerBThread("readFromExisting", 
        { 
           message:["read","only"], 
           number:42
        },
        function(){
            bp.log.info( bp.thread.data );
            bp.log.info( bp.thread.name );
            const evt = bp.sync({request:bp.Event(bp.thread.data.message[0])});
            bp.log.info("static message");
            bp.log.info("last event: " + evt.name);
            bp.log.info( bp.thread.name );
            bp.log.info( bp.thread.data );
            bp.sync({request:bp.Event(bp.thread.data.message[1])});
            bp.sync({request:bp.Event(String(bp.thread.data.number))});
        }
    );
}

if ( dispatch.indexOf("readWrite") > -1 ) {
    bp.registerBThread("readNwrite",
    {
        counter:0,
        eventPrefix: "event-"
    },
    function(){
        for ( let i=0; i<10; i++ ) {
            let evtName = bp.thread.data.eventPrefix + String(i);
            bp.log.info("counter: " + bp.thread.data.counter );
            req(evtName);
            bp.thread.data.counter = bp.thread.data.counter+1;
        }
        bp.log.info("counter: " + bp.thread.data.counter );
    }
    );
}

if ( dispatch.indexOf("writeNew") > -1 ) {
    bp.registerBThread("writeNew",
    {},
    function(){
        bp.thread.data.arr = [];
        for ( let i=0; i<4; i++ ) {
            req("a");
            bp.thread.data.arr.push(String(i));
        }
        bp.log.info( bp.thread.data.arr );
        req( bp.thread.data.arr.join(",") );
    }
    );
}

if ( dispatch.indexOf("replace") > -1 ) {
    bp.registerBThread("replace",
    {name:"the first obj", info:"muhahaha"},
    function(){
        bp.log.info( bp.thread.data );
        req(bp.thread.data.info);
        bp.log.info( bp.thread.data );
        bp.thread.data = { name2:"le obj second", shminfo:"greblaxs"};
        bp.log.info( bp.thread.data );
        req(bp.thread.data.shminfo);
        bp.log.info( bp.thread.data );
    });
}

if ( dispatch.indexOf("create") > -1 ) {
    bp.registerBThread("create",
    function(){
        bp.log.info( bp.thread.data );
        req("pre-set");
        bp.thread.data = {name:"newData"};
        bp.log.info( bp.thread.data );
        req(bp.thread.data.name );
    });
}
