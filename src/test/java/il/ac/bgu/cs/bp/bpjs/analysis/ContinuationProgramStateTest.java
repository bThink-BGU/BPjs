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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import static java.util.Collections.emptySet;
import java.util.concurrent.ExecutorService;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
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
                    + "   var evt = bp.sync({request: bp.Event(\"e\")});\n"
                    + "   bp.log.info('gVar:' + gVar + ' bVar:'+bVar + ' event.name:' + evt.name);"
                    + "   gVar = gVar+1;"
                    + "   bVar = bVar+1;"
                    + "});";
    
    static final String SRC_SHORT = 
                      "var gVar='gVar content';\n" 
                    + "var shadowed='original content (you should not see this)';\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   var f=function(){bp.sync({request: bp.Event(\"e\")});};\n"
                    + "   var fVar='fVar content';\n"
                    + "   var fObjVar={a:'obj->a content'}\n"
                    + "   var shadowed='updated content';\n" 
                    + "   bp.sync({request: bp.Event(\"e\")});\n"
                    + "});";
    
    static final String SRC_MORE_FUNC = 
                      "var gVar='gVar content';\n" 
                    + "var shadowed='original content (you should not see this)';\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   var f=function(){bp.sync({request: bp.Event(\"e\")});};\n"
                    + "   var fVar='fVar content';\n"
                    + "   var fObjVar={a:'obj->a content'}\n"
                    + "   var shadowed='updated content';\n"
                    + "   f()\n"
                    + "});";
    
    static final String SRC_WITH_COMPOUND_VARS = 
                      "var obj={a:1, b:2, f:function(){return 9;}};\n" 
                    + "var obj={a:{f:function(){return 9;}}, k:42};\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   var f=function(){bp.sync({request: bp.Event(\"e\")});};\n"
                    + "   var fVar='fVar content';\n"
                    + "   var fObjVar={a:'obj->a content'}\n"
                    + "   var shadowed='updated content';\n" 
                    + "   bp.sync({request: bp.Event(\"e\")});\n"
                    + "});";
    
    ExecutorService exSvc;
    
    @Test
    public void testCorrectExtraction() throws Exception {
         
        // Generate a continuation
        BProgram bprog = new StringBProgram(SRC);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start(exSvc);
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
        cur = cur.start(exSvc);
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
        cur = cur.start(exSvc);
        BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc);
        
        // Generate Continuation 2
        bprog = new StringBProgram(SRC_WITH_COMPOUND_VARS);
        cur = bprog.setup();
        cur = cur.start(exSvc);
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
        "var dbl=1;\n" +
        "var str='a';\n"+
        "bp.sync({waitFor:bp.Event(\"e\")});\n" +
        "dbl = 42;\n" +
        "str = 'b';\n" +
        "bp.sync({waitFor:bp.Event(\"e\")});\n" +
        "while ( true ) { \n" +
        "    dbl = dbl + 5;\n" +
        "    str = str + 'a';\n" + 
        "    bp.log.info(\"dbl=\" + dbl);\n" +
        "    bp.sync({waitFor:bp.Event(\"e\")});\n" +
        "}});";
    
    @Test
    public void testInequalityLoop() throws Exception {
         
        // Generate snapshot 1
        BProgram bprog = new StringBProgram(SRC_LOOP_UPDATED_VAR);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start(exSvc);
        BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc);
        
        assertEquals( 1.0, sut1.getVisibleVariables().get("dbl"));
        assertEquals( "a", sut1.getVisibleVariables().get("str"));
        
        // Generate snapshot 2, pre-loop
        cur = cur.triggerEvent(new BEvent("e"), exSvc, emptySet());
        snapshot = cur.getBThreadSnapshots().iterator().next();
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut2 = new ContinuationProgramState(nc);
        assertEquals( "b", sut2.getVisibleVariables().get("str"));
        assertEquals( 42.0, sut2.getVisibleVariables().get("dbl"));
        
        // Generate snapshot 3, first loop
        cur = cur.triggerEvent(new BEvent("e"), exSvc, emptySet());
        snapshot = cur.getBThreadSnapshots().iterator().next();
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut3 = new ContinuationProgramState(nc);
        assertEquals( 47.0, sut3.getVisibleVariables().get("dbl"));
        assertEquals( "ba", sut3.getVisibleVariables().get("str"));
        
        // Generate snapshot 4, second loop
        cur = cur.triggerEvent(new BEvent("e"), exSvc, emptySet());
        snapshot = cur.getBThreadSnapshots().iterator().next();
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut4 = new ContinuationProgramState(nc);
        assertEquals( 52.0, sut4.getVisibleVariables().get("dbl"));
        assertEquals( "baa", sut4.getVisibleVariables().get("str"));
        
        assertFalse(sut2.equals(sut1));
    }
    
    
    static final String SRC_LOOP = 
            "var gVar='gVar content';\n" 
          + "var shadowed='original content (you should not see this)';\n" 
          + "bp.registerBThread( \"bt\", function(){\n"
          + "   var f=function(){bp.sync({request: bp.Event(\"e\")});};\n"
          + "   var fVar='fVar content';\n"
          + "   var aVar=[1,2,3,4];\n"
          + "   var fObjVar={a:'obj->a content'}\n"
          + "   var shadowed='updated content';\n" 
          + "   bp.sync({request: bp.Event(\"e\")}); "
          + "   while(true) { "
          + "       var loopLocal='ll';\n "
          + "       bp.log.info('iteration.' + fVar  + ' ' + aVar); \n"
          + "       bp.sync({request: bp.Event(\"e\")}); "
          + "   }\n"
          + "});";
    
    @Test
    public void testLoopSameWhenNoVarChanges() throws Exception {
         
        // Generate snapshot 1
        BProgram bprog = new StringBProgram(SRC_LOOP);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start(exSvc);
        BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sutPre = new ContinuationProgramState(nc);
        
        cur = cur.triggerEvent(new BEvent("e"), exSvc, emptySet());
        snapshot = cur.getBThreadSnapshots().iterator().next();
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sutLoop1 = new ContinuationProgramState(nc);
        
        // Generate more snapshots
        for ( int i=0; i<10; i++ ) {
            cur = cur.triggerEvent(new BEvent("e"), exSvc, emptySet());
            snapshot = cur.getBThreadSnapshots().iterator().next();
            nc = (NativeContinuation) snapshot.getContinuation();
            ContinuationProgramState sutCurLoop = new ContinuationProgramState(nc);

            assertEquals( sutLoop1, sutCurLoop );
            assertNotEquals( sutPre, sutCurLoop );
        }
        
    }
    
    @Test
    public void testEqualityFalse() throws Exception {
         
        // Generate a continuation
        BProgram bprog = new StringBProgram(SRC);
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start(exSvc);
        final BThreadSyncSnapshot snapshot1 = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data of P1
        NativeContinuation nc1 = (NativeContinuation) snapshot1.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc1);
        
        bprog = new StringBProgram(SRC_SHORT);
        cur = bprog.setup();
        cur = cur.start(exSvc);
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
        cur = cur.start(exSvc);
        BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data of P1
        NativeContinuation nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut1 = new ContinuationProgramState(nc);
        
        assertEquals(0, sut1.getFrameIndex());
        
        bprog = new StringBProgram(SRC_MORE_FUNC);
        cur = bprog.setup();
        cur = cur.start(exSvc);
        snapshot = cur.getBThreadSnapshots().iterator().next();
        
        // Read frame data of P1
        nc = (NativeContinuation) snapshot.getContinuation();
        ContinuationProgramState sut2 = new ContinuationProgramState(nc);
        
        assertEquals(1, sut2.getFrameIndex());
    }
    
    @Before
    public void setup() {
        exSvc = ExecutorServiceMaker.makeWithName("Test");
    }
}
