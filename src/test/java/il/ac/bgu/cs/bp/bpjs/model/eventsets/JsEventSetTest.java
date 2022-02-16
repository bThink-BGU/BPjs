/*
 * The MIT License
 *
 * Copyright 2022 michael.
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
package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class JsEventSetTest {

    /**
     * Test of contains method, of class JsEventSet.
     */
    @Test
    public void testContains() {
        BProgram bprog = new ResourceBProgram("model/eventsets/jsEventSet.js");
        bprog.setup();
        JsEventSet esA = bprog.getFromGlobalScope("esA", JsEventSet.class).get();
        JsEventSet esB = bprog.getFromGlobalScope("esB", JsEventSet.class).get();
        
        BPjs.withContext((c)->{
            assertTrue( esA.contains(BEvent.named("AAA")));
            assertTrue( esB.contains(BEvent.named("BBB")));
            assertFalse( esB.contains(BEvent.named("ABB")));
        });
        
    }

    
    /**
     * Test of getName method, of class JsEventSet.
     */
    @Test
    public void testGetName() {
        BProgram bprog = new ResourceBProgram("model/eventsets/jsEventSet.js");
        bprog.setup();
        JsEventSet esA = bprog.getFromGlobalScope("esA", JsEventSet.class).get();
        BPjs.withContext((c)->{
            assertEquals("esA", esA.getName() );
        });
    }

    /**
     * Test of toString method, of class JsEventSet.
     */
    @Test
    public void testToString() {
        BProgram bprog = new ResourceBProgram("model/eventsets/jsEventSet.js");
        bprog.setup();
        JsEventSet esA = bprog.getFromGlobalScope("esA", JsEventSet.class).get();
        
        BPjs.withContext((c)->{
            String toString = esA.toString();
            assertTrue( toString.contains(esA.getName()) );
            assertTrue( toString.contains("JsEventSet") );
        });
    }

    /**
     * Test of equals method, of class JsEventSet.
     */
    @Test
    public void testEquals() {
        BProgram bprog = new ResourceBProgram("model/eventsets/jsEventSet.js");
        bprog.setup();
        JsEventSet esA = bprog.getFromGlobalScope("esA", JsEventSet.class).get();
        JsEventSet esAtag = bprog.getFromGlobalScope("esA_T", JsEventSet.class).get();
        JsEventSet esB = bprog.getFromGlobalScope("esB", JsEventSet.class).get();
        
        BPjs.withContext((c)->{
            assertEquals(esA, esA);
            assertNotEquals(esA, esAtag);
            assertNotEquals(esA, esB);
        });
        
    }    
}
