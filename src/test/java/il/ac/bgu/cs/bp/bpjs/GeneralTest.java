/*
 * The MIT License
 *
 * Copyright 2018 michael.
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
package il.ac.bgu.cs.bp.bpjs;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspection;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import static java.util.stream.Collectors.toSet;
import java.util.stream.IntStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Home for tests which examine BPjs as a whole.
 * @author michael
 */
public class GeneralTest {
    
    /**
     * Testing multiple independent instances of BPjs.
     * 
     * This test seems to block our CI, so disabling for now.
     * 
     * @throws InterruptedException 
     */
    @org.junit.Test
    public void multipleBPjsInstances() throws InterruptedException {
        int rangeStart = 1000;
//      rangeStart = 100; // un-comment for a stress test
        int rangeEnd   = 1010;
        int threadPoolSize = 4;
        
        boolean printResults = true;
        
        Set<? extends Callable<String>> tasks = IntStream.rangeClosed(rangeStart, rangeEnd).mapToObj(i -> {
            return (Callable<String>) () -> {
                ResourceBProgram bprog = new ResourceBProgram("countingBProgram.js");
                BProgramRunner rnr = new BProgramRunner(bprog);
                AtomicReference<String> res = new AtomicReference<>("initial value of task " + i);
                rnr.addListener( new BProgramRunnerListenerAdapter(){
                    @Override
                    public void eventSelected(BProgram bp, BEvent theEvent) {
                        if ( theEvent.getName().startsWith("Result:") ) {
                            res.set(theEvent.getName());
                        }
                    }
                    
                    @Override
                    public void started(BProgram bp) {
                        if ( printResults ) {
                            System.out.println("Task " + i + " started");
                        }
                    }
                    
                    @Override
                    public void ended(BProgram bp) {
                        if ( printResults ) {
                            System.out.println("Task " + i + " ended");
                        }
                    }
                    
                } );
                
                bprog.putInGlobalScope("COUNT", i);
                rnr.run();
                return res.get();
            };
        }).collect( toSet() );
        
        // Init a true multithreading service, since we need 
        // to test concurrency
        ExecutorService svc = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<String>> results = svc.invokeAll(tasks);
        boolean areAllCompleted = results.stream().allMatch( fs -> fs.isDone() );
        
        assertTrue( areAllCompleted );
        
        Set<String> actualFinalEventNames = results.stream()
                                                    .map( TestUtils::safeGet ).collect( toSet() );
        Set<String> expectedFinalEventNames = IntStream.rangeClosed(rangeStart, rangeEnd)
                                                        .mapToObj( i -> "Result: " + i ).collect(toSet());
        
        assertEquals( expectedFinalEventNames, actualFinalEventNames );
        
        if ( printResults ) {
            actualFinalEventNames.stream().sorted().forEach( System.out::println );
        }
        
        svc.shutdown();
        
    }
    
    @Test
    public void testScopesDuringRun() throws Exception {
        ResourceBProgram bprog = new ResourceBProgram("scoping.js");
        BProgramRunner sut = new BProgramRunner(bprog);
        
        sut.addListener( new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.run();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        assertEquals( 
            Arrays.asList("global","undefined", "internalScope1", "b-thread", "internalScope2", "internalScope3"),
            eventLogger.eventNames()
        );
    }
    
    @Test
    public void testScopesDuringVerification() throws Exception {
        ResourceBProgram bprog = new ResourceBProgram("scoping.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        final AtomicReference<List<String>> result = new AtomicReference<>();
        vfr.addInspection(new ExecutionTraceInspection() {
            @Override
            public String title() {
                return "trace test";
            }

            @Override
            public Optional<Violation> inspectTrace(ExecutionTrace aTrace) {
                if ( aTrace.getLastState().noBThreadsLeft() ) {
                    result.set(TestUtils.traceEventNames(aTrace));
                }
                return Optional.empty();
            }
        });
        
        vfr.verify(bprog);
        
        assertEquals( 
            Arrays.asList("global","undefined", "internalScope1", "b-thread", "internalScope2", "internalScope3"),
            result.get()
        );
    }
}
