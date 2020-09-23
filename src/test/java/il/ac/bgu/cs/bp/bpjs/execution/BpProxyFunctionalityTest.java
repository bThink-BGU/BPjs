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
package il.ac.bgu.cs.bp.bpjs.execution;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.isEmbeddedSublist;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspection;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class BpProxyFunctionalityTest {
 
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
    public void analysis_run() {
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
    public void analysis_analyze() throws Exception {
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

