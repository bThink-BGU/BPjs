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
package il.ac.bgu.cs.bp.bpjs.analysis;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.makeBPSS;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class ArrayExecutionTraceTest {
    
    BEvent EVT_1 = new BEvent("EVT1");
    BEvent EVT_2 = new BEvent("EVT2");
    BEvent EVT_3 = new BEvent("EVT3");
    BEvent EVT_4 = new BEvent("EVT4");
    
    /**
     * Test of getLastEvent method, of class ArrayExecutionTrace.
     */
    @Test
    public void testGetLastEvent() {
        
        ArrayExecutionTrace sut = new ArrayExecutionTrace(new StringBProgram(""));
        
        sut.push(makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_1, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_2, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_3, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_4, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        
        assertEquals(EVT_4, sut.getLastEvent().get() );
        
        sut.cycleTo(EVT_3, 2);
        assertEquals(EVT_3, sut.getLastEvent().get() );
    }

    
    /**
     * Test of isCyclic method, of class ArrayExecutionTrace.
     */
    @Test
    public void testgetFinalCycle() {
                
        ArrayExecutionTrace sut = new ArrayExecutionTrace(new StringBProgram(""));
        
        sut.push(makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        assertTrue( sut.getLastEvent().isEmpty() );
        sut.advance(EVT_1, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_2, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_3, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_4, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        
        assertTrue(sut.getFinalCycle().isEmpty());
        
        sut.cycleTo(EVT_3, 2);
        assertEquals(3, sut.getFinalCycle().size());

    }

    /**
     * Test of clear method, of class ArrayExecutionTrace.
     */
    @Test
    public void testClear() {
        
        ArrayExecutionTrace sut = new ArrayExecutionTrace(new StringBProgram(""));
        
        sut.push(makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_1, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_2, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_3, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        sut.advance(EVT_4, makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make())));
        
        assertEquals( 5, sut.getStateCount() );
        
        sut.clear();
        
        assertEquals( 0, sut.getStateCount() );
        assertTrue( sut.getLastEvent().isEmpty() );
        
    }
    
}
