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
package il.ac.bgu.cs.bp.bpjs.verification;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.eventNamesString;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.PrintBProgramListener;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.NotVisitedStore;
import il.ac.bgu.cs.bp.bpjs.verification.requirements.EventNotPresent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static il.ac.bgu.cs.bp.bpjs.TestUtils.traceEventNamesString;
import il.ac.bgu.cs.bp.bpjs.verification.listeners.BriefPrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.verification.requirements.NoDeadlock;

/**
 *
 * @author michael
 */
public class DfsBProgramVerifierTest {
    
    @Test
    public void simpleAAABTrace() throws Exception {
        BProgram program = new SingleResourceBProgram("AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        sut.setRequirement(new EventNotPresent(new BEvent("B")) );
        sut.setVisitedNodeStore(new NotVisitedStore());
        VerificationResult res = sut.verify(program);
        assertTrue( res.isCounterExampleFound() );
        assertEquals("AAAB", traceEventNamesString(res.getCounterExampleTrace(),"") );
    }
    
    @Test 
    public void testAAABRun() throws Exception {
        BProgram program = new SingleResourceBProgram("AAABTrace.js");
        BProgramRunner rnr = new BProgramRunner(program);
        
        rnr.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener eventLogger = rnr.addListener( new InMemoryEventLoggingListener() );
        rnr.start();
        
        eventLogger.getEvents().forEach( System.out::println );
        assertTrue(eventNamesString( eventLogger.getEvents(), "").matches("^(AAAB)+$"));
    }
    
    @Test
    public void deadlockTrace() throws Exception {
        BProgram program = new SingleResourceBProgram("deadlocking.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setRequirement(new NoDeadlock() );
        sut.setVisitedNodeStore(new NotVisitedStore());
        VerificationResult res = sut.verify(program);
        assertTrue( res.isCounterExampleFound() );
        assertEquals("A", traceEventNamesString(res.getCounterExampleTrace(),"") );
    }
    
    @Test 
    public void deadlockRun() throws Exception {
        BProgram program = new SingleResourceBProgram("deadlocking.js");
        BProgramRunner rnr = new BProgramRunner(program);
        
        rnr.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener eventLogger = rnr.addListener( new InMemoryEventLoggingListener() );
        rnr.start();
        
        eventLogger.getEvents().forEach( System.out::println );
        assertTrue(eventNamesString( eventLogger.getEvents(), "").matches("^A$"));
    }
    
}
