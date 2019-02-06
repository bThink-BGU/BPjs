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
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class ExecutionTraceEntryTest {
    
    @Test
    public void testSanity() {
        BProgramSyncSnapshot bpss1 = makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make()));
        BProgramSyncSnapshot bpss2 = makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make()));
        BEvent evt1 = new BEvent("EVT1");
        BEvent evt2 = new BEvent("EVT2");
        
        ExecutionTrace.Entry sut1 = new ExecutionTrace.Entry(bpss1, evt1);
        ExecutionTrace.Entry sut2 = new ExecutionTrace.Entry(bpss2, evt2);
        ExecutionTrace.Entry sut3 = new ExecutionTrace.Entry(bpss2, null);
        
        assertTrue( sut1.equals(sut1) );
        assertFalse( sut3.getEvent().isPresent() );
        assertEquals( bpss1, sut1.getState() );
        assertEquals( evt1, sut1.getEvent().get() );
            
        Set<ExecutionTrace.Entry> evts = new HashSet<>();
        evts.add( sut1 );
        evts.add( sut1 );
        evts.add( sut2 );
        
        assertEquals( 2, evts.size() );
        assertTrue( evts.contains(sut1) );
        assertTrue( evts.contains(sut2) );
    }
    
    @Test
    public void testSetter() {
        BProgramSyncSnapshot bpss1 = makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make()));
        
        BEvent evt1 = new BEvent("EVT1");
        BEvent evt2 = new BEvent("EVT2");
        
        ExecutionTrace.Entry sut1 = new ExecutionTrace.Entry(bpss1, evt1);
        ExecutionTrace.Entry sut2 = new ExecutionTrace.Entry(bpss1, evt2);
        
        assertNotEquals( sut1, sut2 );
        sut2.setEvent(evt1);
        assertEquals( sut1, sut2 );
    }
    
    @Test
    public void testToString() {
        BProgramSyncSnapshot bpss = makeBPSS(new MockBThreadSyncSnapshot(SyncStatement.make()));
        BEvent evt = new BEvent("EVT");
        ExecutionTrace.Entry sut = new ExecutionTrace.Entry(bpss, evt);
        
        assertTrue( sut.toString().contains("EVT") );
        
    }
}
