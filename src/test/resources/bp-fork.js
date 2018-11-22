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

var EVT_C = bp.Event("childPrint");
var EVT_P = bp.Event("parentPrint");

bp.registerBThread("source", function(){
    var myInt = 0;
    var myString = "baseString";
    var myObj = {
        forkReturn: 0
    };
    
    if ( bp.fork() ) {
        myInt=myInt+2;
        myString = myString+" - forkedString";
        myObj.forkReturn=1;
        myObj.childMessage="Child b-t";
        bp.info.log("XX IN Child");
    } else {
        bp.log.info("XX IN Parent");
        myObj.forkReturn=2;
        myInt=myInt+1;
        myString=myString+" - parentString";
        myObj.parentMessage="Parent b-t";
    }
    
    bp.sync({
        request: myObj.forkReturn===1? EVT_C : EVT_P
    });
    
    bp.log.info("myInt: " + myInt);
    bp.log.info("myString: " + myString);
    bp.log.info("myObj: " + JSON.stringify(myObj) );
    
});