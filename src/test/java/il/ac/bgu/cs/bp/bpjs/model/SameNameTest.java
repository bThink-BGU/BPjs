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

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspection;
import il.ac.bgu.cs.bp.bpjs.analysis.ForgetfulVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class SameNameTest {

    @Test
    public void bthreadsWithSameName_Run(){
        BProgram sut = new ResourceBProgram("bthreads-with-same-name.js");
        BProgramRunner rnr = new BProgramRunner(sut);
        InMemoryEventLoggingListener lsn = rnr.addListener(new InMemoryEventLoggingListener());
        
        rnr.run();
        
        List<String> exA = Arrays.asList("A","B","C");
        assertEquals(exA, lsn.eventNames().stream().filter(exA::contains).collect(toList()));
        
        List<String> exB = Arrays.asList("X","Y","Z");
        assertEquals(exB, lsn.eventNames().stream().filter(exB::contains).collect(toList()));
    }

    @Test
    public void bthreadsWithSameName_analysis() throws Exception{
        BProgram sut = new ResourceBProgram("bthreads-with-same-name.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        
        final AtomicInteger runCount = new AtomicInteger(0);
        vfr.addInspection(ExecutionTraceInspection.named("Finite Runs", (t) -> {
            if ( !t.isCyclic() && t.getLastState().noBThreadsLeft() ) {
                List<String> traceEventNames = TestUtils.traceEventNames(t);
                assertTrue(TestUtils.isEmbeddedSublist(Arrays.asList("A","B","C"), traceEventNames));
                assertTrue(TestUtils.isEmbeddedSublist(Arrays.asList("X","Y","Z"), traceEventNames));
                runCount.incrementAndGet();
            }
            return Optional.empty();
        }));
        vfr.setVisitedStateStore(new ForgetfulVisitedStateStore()); // needed since we search runs, not states.
        vfr.verify(sut);
        
        // We expect 20 since this is a "6 choose 3" situation: At each sync, the 
        // arbiter can advance the ABC or the XYZ thread. Both have 3 steps, leading to
        // a total of 6 steps. So the amount of runs equals to the number of options
        // the arbiter has to choose when to advance, say, the ABC thread. 3 steps out
        // of a total of 6, so "6 choose 3", C(6,3).
        assertEquals(20, runCount.get());
    }
    
}
