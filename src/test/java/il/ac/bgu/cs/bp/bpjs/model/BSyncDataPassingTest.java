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
package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Testing event data field population by bp.sync. 
 * Also, testing {@link PrioritizedBSyncEventSelectionStrategy}.
 * 
 * @author michael
 */
public class BSyncDataPassingTest {
    
    @Test
    public void priorityDataTest_noBlocking() {
        BProgram bp = new SingleResourceBProgram("BSyncDataPassingTest.js");
        bp.putInGlobalScope("block30", false);
        
        bp.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
        BProgramRunner rnr = new BProgramRunner(bp);
        InMemoryEventLoggingListener eventsLogger = rnr.addListener(new InMemoryEventLoggingListener());
        rnr.addListener( new PrintBProgramRunnerListener() );
        
        rnr.run();
        
        assertEquals(
            Arrays.asList("p30", "p20", "p10", "p3", "p2", "p1"),
            eventsLogger.eventNames()
        );
        
    }
    
    @Test
    public void priorityDataTest_blocking() {
        BProgram bp = new SingleResourceBProgram("BSyncDataPassingTest.js");
        bp.putInGlobalScope("block30", true);
        
        bp.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy(17));
        BProgramRunner rnr = new BProgramRunner(bp);
        InMemoryEventLoggingListener eventsLogger = rnr.addListener(new InMemoryEventLoggingListener());
        rnr.addListener( new PrintBProgramRunnerListener() );
        
        rnr.run();
        
        assertEquals(
            Arrays.asList("p20", "p10", "p3", "p2", "p1"),
            eventsLogger.eventNames()
        );
        
    }
}
