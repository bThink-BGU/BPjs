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
package il.ac.bgu.cs.bp.bpjs.model;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.traceEventNames;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspection;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.DetectedSafetyViolation;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class BProgramStoreTest {
    
    /**
     * Testing a simple run, with 1 b-thread talking to itself.
     */
    @Test
    public void sanityTest_run() {
        BProgram sut = new ResourceBProgram("bp_store/bpStore_single.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());
        
        rnr.setBProgram(sut);
        rnr.run();

        assertEquals( List.of("0", "1", "2", "3"), evtListener.eventNames() );
    }
    
    @Test
    public void sanityTest_dfs() throws Exception {
        final AtomicBoolean called = new AtomicBoolean();
        
        BProgram sut = new ResourceBProgram("bp_store/bpStore_single.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.addInspection( ExecutionTraceInspection.named("linear", (t) -> {
            if ( t.getLastState().noBThreadsLeft() ) {
                called.set(true);
                assertEquals(
                    List.of("0", "1", "2", "3"),
                    traceEventNames(t)
                );
            }
            return Optional.empty();
        }));
        
        vfr.verify(sut);
        assertTrue( called.get() );   
    }
    
    @Test
    public void readWriteTest() {
        BProgram sut = new ResourceBProgram("bp_store/bpStore_multi_simple.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());
        
        rnr.setBProgram(sut);
        rnr.run();
        
        List<String> eventNames = evtListener.eventNames();
        assertEquals("4/5", eventNames.get(eventNames.size()-1) );
    }
    
    @Test
    public void readWriteTest_dfs() throws Exception {
        final AtomicBoolean called = new AtomicBoolean();
        final AtomicInteger counter = new AtomicInteger();
        
        BProgram sut = new ResourceBProgram("bp_store/bpStore_multi_simple.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.addInspection( ExecutionTraceInspection.named("linear", (t) -> {
            if ( t.getLastState().noBThreadsLeft() ) {
                called.set(true);
                counter.incrementAndGet();
                List<String> names = traceEventNames(t);
                assertEquals(
                    "4/5",
                    names.get(names.size()-1) 
                );
            }
            return Optional.empty();
        }));
        
        vfr.verify(sut);
        assertTrue( called.get() );
        System.out.println("verification call #:" + counter.get());
    }
    
    /**
     * Testing a simple run, with 1 b-thread talking to itself.
     */
    @Test
    public void multipleBThreadHarmony_run() {
        BProgram sut = new ResourceBProgram("bp_store/bpStore_multi.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());
        
        rnr.setBProgram(sut);
        rnr.run();
        
        assertEquals( "(5,5)", evtListener.eventNames().get(evtListener.eventNames().size()-1));
    }
    
    @Test
    public void conflict_run() {
        AtomicBoolean called = new AtomicBoolean();
        BProgram sut = new ResourceBProgram("bp_store/bpStore_conflict.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.addListener( new BProgramRunnerListenerAdapter() {
            @Override
            public void assertionFailed(BProgram bp, SafetyViolationTag theFailedAssertion) {
                called.set(true);
                assertTrue( theFailedAssertion instanceof StorageConflictViolation );
                StorageConflictViolation scv = (StorageConflictViolation) theFailedAssertion;
                StorageConsolidationResult.Conflict conflict = scv.getConflict();
                assertEquals( Set.of("c"), conflict.conflicts.keySet() );
            }
            
        });
        rnr.setBProgram(sut);
        rnr.run();
        assertTrue( called.get() );
    }
    
    @Test
    public void conflict_dfs() throws Exception {
        BProgram sut = new ResourceBProgram("bp_store/bpStore_conflict.js");
        
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        VerificationResult res = vfr.verify(sut);
        
        assertTrue( res.isViolationFound() );
        assertTrue( res.getViolation().get() instanceof DetectedSafetyViolation );
        DetectedSafetyViolation dsv = (DetectedSafetyViolation)res.getViolation().get();
        StorageConflictViolation scv = (StorageConflictViolation) dsv.getDetectedViolation();
        assertEquals( Set.of("c"), scv.getConflict().conflicts.keySet() );
    }
    
    @Test
    public void preRunStorageTest() {
        BProgram sut = new ResourceBProgram("bp_store/bpStore_pre_run.js");
        BProgramRunner rnr = new BProgramRunner();
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());
                
        rnr.setBProgram(sut);
        rnr.run();
        assertEquals( List.of("Josh", "Agenta Falskog"), evtListener.eventNames() );
    }
    
    @Test
    public void lastLeg_run() {
        BProgram sut = new ResourceBProgram("bp_store/bpStore_lastLegChanges.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());
        
        rnr.setBProgram(sut);
        rnr.run();
        
        assertEquals( List.of("A","B","C","D"), evtListener.eventNames() );
    }
    
    
}
