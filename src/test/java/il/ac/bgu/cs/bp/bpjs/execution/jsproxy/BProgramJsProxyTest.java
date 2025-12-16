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
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.bprogramio.log.BpLog;
import static il.ac.bgu.cs.bp.bpjs.TestUtils.isEmbeddedSublist;
import il.ac.bgu.cs.bp.bpjs.analysis.BThreadSnapshotVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import org.junit.Test;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.traceEventNamesString;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspection;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.DeadlockViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author michael
 */
public class BProgramJsProxyTest {
    
    @Test
    public void randomProxyTest() throws InterruptedException {
        BProgram sut = new ResourceBProgram("RandomProxy.js");
        
        new BProgramRunner(sut).run();
        Double boolCount = sut.getFromGlobalScope("boolCount", Double.class).get();
        assertEquals(500.0, boolCount, 100);
        
        Double intCount = sut.getFromGlobalScope("intCount", Double.class).get();
        assertEquals(500.0, intCount, 100);
        
        Double floatCount = sut.getFromGlobalScope("floatCount", Double.class).get();
        assertEquals(500.0, floatCount, 100);
    }

    @Test
    public void logLevelProxyTest() throws InterruptedException {
        
        BProgram sut = new ResourceBProgram("RandomProxy.js");
        
        new BProgramRunner(sut).run();
        String logLevel1 = sut.getFromGlobalScope("logLevel1", String.class).get();
        assertEquals(BpLog.LogLevel.Off.name(), logLevel1);
        
        String logLevel2 = sut.getFromGlobalScope("logLevel2", String.class).get();
        assertEquals(BpLog.LogLevel.Warn.name(), logLevel2);
    }

    @Test
    public void newLogImpProxyTest() throws InterruptedException {

        BProgram sut = new ResourceBProgram("RandomProxy.js");

        new BProgramRunner(sut).run();
        String logLevel1 = sut.getFromGlobalScope("logLevel1", String.class).get();
        assertEquals(BpLog.LogLevel.Off.name(), logLevel1);

        String logLevel2 = sut.getFromGlobalScope("logLevel2", String.class).get();
        assertEquals(BpLog.LogLevel.Warn.name(), logLevel2);
    }
    
    @Test
    public void testEquality() {
        BProgram bprog = new ResourceBProgram("RandomProxy.js");
        new BProgramRunner(bprog).run();
        BProgramJsProxy sut1 = bprog.getFromGlobalScope("bp", BProgramJsProxy.class).get();
        assertEquals( sut1, sut1 );
        assertNotEquals(sut1, null);
        assertNotEquals(sut1, "String");
        
        bprog = new ResourceBProgram("RandomProxy.js");
        new BProgramRunner(bprog).run();
        BProgramJsProxy sut2 = bprog.getFromGlobalScope("bp", BProgramJsProxy.class).get();
        assertEquals( sut1, sut2 );
        
        Map<String, BProgramJsProxy> map = new HashMap<>();
        map.put("sut1", sut1);
        map.put("sut2", sut2);
        
        assertSame( sut1, map.get("sut1") );
        assertSame( sut2, map.get("sut2") );
    }
    
    @Test
    public void deadlockSameThread() throws Exception{
        BProgram bpr = new ResourceBProgram("bpsync-blockrequest.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setVisitedStateStore(new BThreadSnapshotVisitedStateStore());
        VerificationResult res = sut.verify(bpr);
        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DeadlockViolation);
        assertEquals("sampleEvent", traceEventNamesString(res.getViolation().get().getCounterExampleTrace(), ""));
        
        BProgram bprPred = new ResourceBProgram("bpsync-blockrequestPredicate.js");
        res = sut.verify(bprPred);
        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DeadlockViolation);
        assertEquals("sampleEvent", traceEventNamesString(res.getViolation().get().getCounterExampleTrace(), ""));
    }
    
    @Test
    public void formattedLogFromGeneralScope() {
        String src = "let s = new Set()\n" +
                        "s.add('a')\n" +
                        "bp.log.info(\"{0}\", s)";
        BProgram bprog = new StringBProgram(src);
        
        BProgramRunner rnr = new BProgramRunner(bprog);
        
        rnr.run();
    }
    
    
    @Test
    public void testGetTime() throws InterruptedException {
        BProgram sut = new ResourceBProgram("execution/getTimeTest.js");
        long timePre = System.currentTimeMillis();
        new BProgramRunner(sut).run();
        long timePost = System.currentTimeMillis();
        Long actual = sut.getFromGlobalScope("theTime", Long.class).get();
        assertTrue( (actual>=timePre) && (actual<=timePost) );
    }
    
    @Test
    public void testGetName() throws InterruptedException {
        
        BProgram sut = new ResourceBProgram("execution/bthreadData_name.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());
        
        rnr.setBProgram(sut);
        rnr.run();
        
        assertEquals( Set.of("hello", "world", "b-thread","names"),
                      new TreeSet<>(evtListener.eventNames()));
        
    }
    
    @Test
    public void testBThreadData_Read() throws InterruptedException {
         runBThreadDataTest("readFromExisting", List.of("read","only","42") );
    }
    
    @Test
    public void testBThreadData_ReadWrite() throws InterruptedException {
         runBThreadDataTest("readWrite", 
             IntStream.range(0, 10).mapToObj(i->"event-" + i).collect( toList() )
         );
    }
    
    @Test
    public void testBThreadData_WriteNew() throws InterruptedException {
         runBThreadDataTest("writeNew", 
             List.of("a","a","a","a","0,1,2,3")
         );
    }
    
    @Test
    public void testBThreadData_Replace() throws InterruptedException {
         runBThreadDataTest("replace", 
             List.of("muhahaha","greblaxs")
         );
    }
    
    @Test
    public void testBThreadData_Create() throws InterruptedException {
         runBThreadDataTest("create", 
             List.of("pre-set","newData")
         );
    }
        
    @Test
    public void analysisRun() {
        BProgram sut = new ResourceBProgram("execution/bthreadData_analysis.js");
        
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());

        rnr.setBProgram(sut);
        rnr.run();
        assertTrue( isEmbeddedSublist(List.of("a","b","c","d"), evtListener.eventNames()));
        assertTrue( isEmbeddedSublist(List.of("1","2","3","4"), evtListener.eventNames()));
    
    }

    @Test
    public void analysisAnalyze() throws Exception {
        BProgram sut = new ResourceBProgram("execution/bthreadData_analysis.js");
        
        final AtomicInteger counter = new AtomicInteger(0);
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.addInspection(ExecutionTraceInspection.named("allEventsDoneOnce", (t) -> {
            if ( t.getLastState().noBThreadsLeft() ) {
                List<String> eventNames = t.getNodes().stream().map( n -> n.getEvent() )
                    .filter( Optional::isPresent )
                    .map( e -> e.get().getName() )
                    .collect( toList() );
                assertTrue( isEmbeddedSublist(List.of("a","b","c","d"), eventNames));
                assertTrue( isEmbeddedSublist(List.of("1","2","3","4"), eventNames));
                counter.incrementAndGet();
            }
            return Optional.empty();
        }));
        
        VerificationResult res = vfr.verify(sut);
        assertFalse( res.isViolationFound() );
        assertTrue( counter.intValue() > 1 );
        System.out.println("counter.intValue: " + counter.intValue());
        System.out.println("Counts: " + res.getScannedEdgesCount() + "/" + res.getScannedStatesCount() );
    }
  
    @Test
    public void NullEventSetTest() {
        BProgram sut = new ResourceBProgram("nullEventSetTest.js");

        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());

        rnr.setBProgram(sut);
        try {
            rnr.run();
            fail("Program should have thrown an exception about null event set");
        } catch(RuntimeException e) {
            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("first"));
        }
    }

    @Test
    public void syncOutOfBThreadExecTest() {
        BProgram sut = new ResourceBProgram("execution/jsproxy/syncFromGlobalScope.js");
        
        final AtomicReference<String> errorMessage = new AtomicReference<>();
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener( new BProgramRunnerListenerAdapter(){
            @Override
            public void error(BProgram bp, Exception ex) {
                errorMessage.set(ex.getMessage());
            }
        });
        rnr.setBProgram(sut);
        
        rnr.run();
        var errMsg = errorMessage.get();
        assertNotNull("Runtime listener should have captured the error message", errMsg);
        assertTrue(errMsg.toLowerCase().contains("bp.sync"));
        assertTrue(errMsg.toLowerCase().contains("forbidden"));
        assertTrue(errMsg.toLowerCase().contains("outside of a b-thread"));
        
    }
    
    @Test
    public void syncOutOfBThreadAnalyzeTest() throws Exception {
        BProgram sut = new ResourceBProgram("execution/jsproxy/syncFromGlobalScope.js");
        var vfr = new DfsBProgramVerifier(sut);
        final AtomicReference<String> errorMessage = new AtomicReference<>();
        var l = (DfsBProgramVerifier.ProgressListener) (Violation aViolation, DfsBProgramVerifier vfr1) -> {
            errorMessage.set(aViolation.decsribe().toLowerCase());
            return false;
        };
        vfr.setProgressListener(l);
        var res = vfr.verify(sut);
        assertTrue(errorMessage.get().contains("bp.sync"));
        assertTrue(errorMessage.get().contains("forbidden"));
        assertTrue(errorMessage.get().contains("outside of a b-thread"));           
        
        assertTrue( res.isViolationFound() );
      
        String errorMessageFromViolation = res.getViolation().get().decsribe().toLowerCase();      
        assertTrue(errorMessageFromViolation.contains("bp.sync"));
        assertTrue(errorMessageFromViolation.contains("forbidden"));
        assertTrue(errorMessageFromViolation.contains("outside of a b-thread"));           
    }



    @Test
    public void threadOutOfBThreadExecTest() {
        BProgram sut = new ResourceBProgram("execution/jsproxy/bthreadFromGlobalScope.js");

        final AtomicReference<String> errorMessage = new AtomicReference<>();
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener( new BProgramRunnerListenerAdapter(){
            @Override
            public void error(BProgram bp, Exception ex) {
                errorMessage.set(ex.getMessage());
            }
        });
        rnr.setBProgram(sut);

        rnr.run();
        var errMsg = errorMessage.get();
        assertNotNull("Runtime listener should have captured the error message", errMsg);
        assertTrue(errMsg.toLowerCase().contains("bp.thread"));
        assertTrue(errMsg.toLowerCase().contains("forbidden"));
        assertTrue(errMsg.toLowerCase().contains("outside of a b-thread"));

    }

    @Test
    public void threadOutOfBThreadAnalyzeTest() throws Exception {
        BProgram sut = new ResourceBProgram("execution/jsproxy/bthreadFromGlobalScope.js");
         var vfr = new DfsBProgramVerifier(sut);
        final AtomicReference<String> errorMessage = new AtomicReference<>();
        var l = (DfsBProgramVerifier.ProgressListener) (Violation aViolation, DfsBProgramVerifier vfr1) -> {
            errorMessage.set(aViolation.decsribe().toLowerCase());
            return false;
        };
        vfr.setProgressListener(l);
        var res = vfr.verify(sut);
        
        assertTrue(errorMessage.get().contains("bp.thread"));
        assertTrue(errorMessage.get().contains("forbidden"));
        assertTrue(errorMessage.get().contains("outside of a b-thread"));           
        
        assertTrue( res.isViolationFound() );
      
        String errorMessageFromViolation = res.getViolation().get().decsribe().toLowerCase();      
        assertTrue(errorMessageFromViolation.contains("bp.thread"));
        assertTrue(errorMessageFromViolation.contains("forbidden"));
        assertTrue(errorMessageFromViolation.contains("outside of a b-thread"));   
        
    }

    private void runBThreadDataTest(String dispatch, List<String> expectedNames ) {
        BProgram sut = new ResourceBProgram("execution/bthreadData_dataObj.js");
        sut.putInGlobalScope("dispatch", dispatch);
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        var evtListener = rnr.addListener(new InMemoryEventLoggingListener());
        
        rnr.setBProgram(sut);
        rnr.run();

        System.out.println(evtListener.eventNames());
        assertEquals( expectedNames, evtListener.eventNames() );
    }
}
