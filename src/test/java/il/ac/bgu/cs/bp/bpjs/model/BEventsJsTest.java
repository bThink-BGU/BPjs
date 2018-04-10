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
package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author michael
 */
public class BEventsJsTest {
    
    BProgram prog;
    Map<String, BEvent> events = new HashMap<>();
    
    @Before
    public void setUp() throws InterruptedException {
        prog = new BProgram() {
            @Override
            protected void setupProgramScope(Scriptable aScope) {
                aScope.put("events", aScope, Context.javaToJS(events, aScope));                
                evaluate( "events.put('nameOnly1',     bp.Event('nameOnly'));\n"
                        + "events.put('nameOnly2',     bp.Event('nameOnly'));\n"
                        + "events.put('nameOnly-diff', bp.Event('nameOnly-diff'));\n"
                        + "events.put('withData1',           bp.Event('withData',{a:'a',b:'b',c:700}));\n"
                        + "events.put('withData2',           bp.Event('withData',{a:'a',b:'b',c:700}));\n"
                        + "events.put('withData2-reordered', bp.Event('withData',{b:'b',a:'a',c:700}));\n"
                        + "events.put('withData-diff1', bp.Event('withDataX',{a:'a',b:'b',c:700}));\n"
                        + "events.put('withData-diff2', bp.Event('withData',{b:'b',c:700}));\n"
                        + "events.put('withData-diff3', bp.Event('withData',{a:'a',b:'b',c:700,d:'x'}));\n"
                        + "events.put('withData-diff4', bp.Event('withData',{a:'x',b:'b',c:700}));\n"
                        + "events.put('withData-rec',   bp.Event('withData',{a:'x',b:'b',child:{a:2,b:'b'}}));\n"
                        + "events.put('withData-rec2',  bp.Event('withData',{a:'x',b:'b',child:{a:2,b:'b'}}));\n"
                        + "events.put('withPrimitiveData1',      bp.Event('withPrimitiveData',12));\n"
                        + "events.put('withPrimitiveData2',      bp.Event('withPrimitiveData',12));\n"
                        + "events.put('withPrimitiveData-diff1', bp.Event('withPrimitiveData',13));\n"
                        + "events.put('withPrimitiveData-diff2', bp.Event('withPrimitiveData','string'));\n"
                        + "events.put('withPrimitiveData-diff3', bp.Event('withPrimitiveData',{}));\n"
                        + "events.put('withPrimitiveData-diff4', bp.Event('withPrimitiveData',function(p){return p;}));\n"
                        + "events.put('e', bp.Event('e'));\n"
                        + "events.put('m1d', bp.Event('metaE',bp.Event('e')));\n" 
                        + "events.put('m2d', bp.Event('metaE',bp.Event('e')));\n" 
                        + "events.put('m1i', bp.Event('metaE',{e:bp.Event('e'), v:'js-string'}));\n" 
                        + "events.put('m2i', bp.Event('metaE',{e:bp.Event('e'), v:'js-string'}));"
                        ,
                        "inline script" );
            }
        };
        new BProgramRunner(prog).run();
    }
    

    @Test
    public void testContains_Name() throws InterruptedException {
        BEvent nameOnly1 = events.get("nameOnly1");
        BEvent nameOnly2 = events.get("nameOnly2");
        BEvent nameOnlyDiff = events.get("nameOnly-diff");
        
        assertTrue( nameOnly1.contains(nameOnly1) );
        assertTrue( nameOnly1.contains(nameOnly2) );
        assertTrue( nameOnly2.contains(nameOnly1) );
        assertFalse( nameOnly2.contains(nameOnlyDiff) );
    }
    
    @Test
    public void testContains_Object() throws Exception {
        BEvent withData1 = events.get("withData1");
        BEvent withData2 = events.get("withData2");
        BEvent withData2Ordered = events.get("withData2-reordered");
        BEvent withDataDiff1 = events.get("withData-diff1");
        BEvent withDataDiff2 = events.get("withData-diff2");
        BEvent withDataDiff3 = events.get("withData-diff3");
        BEvent withDataDiff4 = events.get("withData-diff4");
        BEvent withDataRec = events.get("withData-rec");
        BEvent withDataRec2 = events.get("withData-rec2");
        
        assertTrue( withData1.contains(withData1) );
        assertTrue( withData1.contains(withData2) );
        assertTrue( withData2.contains(withData1) );
        
        assertTrue( withData2Ordered.contains(withData1) );
        assertTrue( withData1.contains(withData2Ordered) );
        
        assertFalse( withData1.contains(withDataDiff1) );
        assertFalse( withData1.contains(withDataDiff2) );
        assertFalse( withData1.contains(withDataDiff3) );
        assertFalse( withData1.contains(withDataDiff4) );
        assertFalse( withData1.contains(withDataRec) );
        
        assertTrue( withDataRec2.contains(withDataRec) );
        
    }

    @Test
    public void testContains_Primitives() throws Exception {
        BEvent withPrimitiveData1 = events.get("withPrimitiveData1");
        BEvent withPrimitiveData2 = events.get("withPrimitiveData2");
        BEvent withPrimitiveDataDiff1 = events.get("withPrimitiveData-diff1");
        BEvent withPrimitiveDataDiff2 = events.get("withPrimitiveData-diff2");
        BEvent withPrimitiveDataDiff3 = events.get("withPrimitiveData-diff3");
        BEvent withPrimitiveDataDiff4 = events.get("withPrimitiveData-diff4");
        
        assertTrue( withPrimitiveData1.contains(withPrimitiveData1) );
        assertTrue( withPrimitiveData1.contains(withPrimitiveData2) );
        assertTrue( withPrimitiveData2.contains(withPrimitiveData1) );
        
        assertFalse( withPrimitiveData1.contains(withPrimitiveDataDiff1) );
        assertFalse( withPrimitiveData1.contains(withPrimitiveDataDiff2) );
        assertFalse( withPrimitiveData1.contains(withPrimitiveDataDiff3) );
        assertFalse( withPrimitiveData1.contains(withPrimitiveDataDiff4) );
        
        assertFalse( withPrimitiveDataDiff1.contains(withPrimitiveData1) );
        assertFalse( withPrimitiveDataDiff2.contains(withPrimitiveData1) );
        assertFalse( withPrimitiveDataDiff3.contains(withPrimitiveData1) );
        assertFalse( withPrimitiveDataDiff4.contains(withPrimitiveData1) );
    }
    
    @Test
    public void testMetaObjects() throws Exception {
        BEvent m1d = events.get("m1d");
        BEvent m2d = events.get("m2d");
        BEvent m1i = events.get("m1i");
        BEvent m2i = events.get("m2i");
        
        assertTrue( m1d.contains(m1d) );
        assertTrue( m1i.contains(m1i) );
        
        assertTrue( m1d.contains(m2d) );
        assertTrue( m1i.contains(m2i) );
        assertTrue( m2d.contains(m1d) );
        assertTrue( m2i.contains(m1i) );
 
    }
  
}
