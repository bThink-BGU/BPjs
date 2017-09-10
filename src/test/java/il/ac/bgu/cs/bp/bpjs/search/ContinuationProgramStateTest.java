/*
 * The MIT License
 *
 * Copyright 2017 michael.
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
package il.ac.bgu.cs.bp.bpjs.search;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.NativeContinuation;

/**
 *
 * @author michael
 */
public class ContinuationProgramStateTest {
    
    static final String SRC = 
                      "var gVar='gVar content';\n" 
                    + "var shadowed='original content (you should not see this)';\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   var fVar='fVar content';\n"
                    + "   var fDblVar1=1.42\n"
                    + "   var fDblVar2=2.42\n"
                    + "   var fObjVar={a:'obj->a content'}\n"
                    + "   var fVar2='fVar2 content';\n"
                    + "   var shadowed='updated content';\n" 
                    + "   var evt = bsync({request: bp.Event(\"e\")});\n"
                    + "   bp.log.info('gVar:' + gVar + ' bVar:'+bVar + ' event.name:' + evt.name);"
                    + "   gVar = gVar+1;"
                    + "   bVar = bVar+1;"
                    + "});";
    
    static final String SRC_SHORT = 
                      "var gVar='gVar content';\n" 
                    + "var shadowed='original content (you should not see this)';\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   var f=function(){bsync({request: bp.Event(\"e\")});};\n"
                    + "   var fVar='fVar content';\n"
                    + "   var fObjVar={a:'obj->a content'}\n"
                    + "   var shadowed='updated content';\n" 
                    + "   bsync({request: bp.Event(\"e\")});\n"
                    + "});";
    
    static final String SRC_MORE_FUNC = 
                      "var gVar='gVar content';\n" 
                    + "var shadowed='original content (you should not see this)';\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   var f=function(){bsync({request: bp.Event(\"e\")});};\n"
                    + "   var fVar='fVar content';\n"
                    + "   var fObjVar={a:'obj->a content'}\n"
                    + "   var shadowed='updated content';\n"
                    + "   f()\n"
                    + "});";
    
    static final String SRC_LOOP = 
            "var gVar='gVar content';\n" 
          + "var shadowed='original content (you should not see this)';\n" 
          + "bp.registerBThread( \"bt\", function(){\n"
          + "   var f=function(){bsync({request: bp.Event(\"e\")});};\n"
          + "   var fVar='fVar content';\n"
          + "   var fObjVar={a:'obj->a content'}\n"
          + "   var shadowed='updated content';\n" 
          + "   while(true) { bsync({request: bp.Event(\"e\")}); }\n"
          + "});";
    

    static final String SRC_WITH_COMPOUND_VARS = 
                      "var obj={a:1, b:2, f:function(){return 9;}};\n" 
                    + "var obj={a:{f:function(){return 9;}}, k:42};\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   var f=function(){bsync({request: bp.Event(\"e\")});};\n"
                    + "   var fVar='fVar content';\n"
                    + "   var fObjVar={a:'obj->a content'}\n"
                    + "   var shadowed='updated content';\n" 
                    + "   bsync({request: bp.Event(\"e\")});\n"
                    + "});";
    
    @Test
    public void testCorrectExtraction() throws Exception {
         
        // Generate a continuation
        BProgram bprog = new StringBProgram(SRC);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start();
        final BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut = new ContinuationProgramState(nc);
        
        assertEquals(0, sut.getFrameIndex());
        assertEquals("gVar content", sut.getVisibleVariables().get("gVar"));
        assertEquals("fVar content", sut.getVisibleVariables().get("fVar"));
        assertEquals("updated content", sut.getVisibleVariables().get("shadowed"));
        assertEquals(2.42, sut.getVisibleVariables().get("fDblVar2"));
    }
    
    @Test
    public void testEqualityTrue() throws Exception {
         
        // Generate a continuation
        BProgram bprog = new StringBProgram(SRC);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start();
        final BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc);
        ContinuationProgramState sut2 = new ContinuationProgramState(nc);
        
        assertTrue(sut1.equals(sut2));
    }
    
    @Test
    public void testEqualityComplexObj() throws Exception {
         
        // Generate continuation 1
        BProgram bprog = new StringBProgram(SRC_WITH_COMPOUND_VARS);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start();
        BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc);
        
        // Generate Continuation 2
        bprog = new StringBProgram(SRC_WITH_COMPOUND_VARS);
        cur = bprog.setup();
        cur = cur.start();
        snapshot = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut2 = new ContinuationProgramState(nc);
        
        assertTrue(sut1.equals(sut1)); // sanity
        assertTrue(sut1.equals(sut2));
        assertTrue(sut2.equals(sut1));
    }
   
    static final String SRC_LOOP_UPDATED_VAR = 
        "bp.registerBThread( function(){ \n" +
        "var i=0;\n" +
        "bsync({waitFor:bp.Event(\"e\")});\n" +
        "i += 42;\n" +
        "bp.log.info(\"pre-loop: i=\" + i);\n" +
        "bsync({waitFor:bp.Event(\"e\")});\n" +
        "while ( true ) { \n" +
        "    i = i + 5;\n" +
        "    bp.log.info(\"i=\" + i);\n" +
        "    bsync({waitFor:bp.Event(\"e\")});\n" +
        "}});";
    
//    @Test
    public void testInequalityLoop() throws Exception {
         
        // Generate snapshot 1
        BProgram bprog = new StringBProgram(SRC_LOOP_UPDATED_VAR);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start();
        BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc);
        
        assertEquals( 0.0, sut1.getVisibleVariables().get("i"));
        
        // Generate snapshot 2, pre-loop
        cur = cur.triggerEvent(new BEvent("e"));
        snapshot = cur.getBThreadSnapshots().iterator().next();
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut2 = new ContinuationProgramState(nc);
        assertEquals( 42.0, sut2.getVisibleVariables().get("i"));
        
        // Generate snapshot 3, first loop
        cur = cur.triggerEvent(new BEvent("e"));
        snapshot = cur.getBThreadSnapshots().iterator().next();
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut3 = new ContinuationProgramState(nc);
        assertEquals( 47.0, sut3.getVisibleVariables().get("i"));
        
        assertFalse(sut2.equals(sut1));
    }
    
    @Test
    public void testEqualityFalse() throws Exception {
         
        // Generate a continuation
        BProgram bprog = new StringBProgram(SRC);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start();
        final BThreadSyncSnapshot snapshot1 = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data of P1
        NativeContinuation nc1 = (NativeContinuation) snapshot1.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc1);
        
        bprog = new StringBProgram(SRC_SHORT);
        cur = bprog.setup();
        cur = cur.start();
        final BThreadSyncSnapshot snapshot2 = cur.getBThreadSnapshots().iterator().next();
        NativeContinuation nc2 = (NativeContinuation) snapshot2.getContinuation();
        ContinuationProgramState sut2 = new ContinuationProgramState(nc2);
        
        assertFalse(sut1.equals(sut2));
        assertTrue( sut1.getProgramCounter() > sut2.getProgramCounter() );
    }
    
    @Test
    public void testStackHeight() throws Exception {
         
        // Generate a continuation
        BProgram bprog = new StringBProgram(SRC_SHORT);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start();
        BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data of P1
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc);
        
        assertEquals(0, sut1.getFrameIndex());
        
        bprog = new StringBProgram(SRC_MORE_FUNC);
        cur = bprog.setup();
        cur = cur.start();
        snapshot = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data of P1
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut2 = new ContinuationProgramState(nc);
        
        assertEquals(1, sut2.getFrameIndex());
    }
}
