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
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.AbstractEventSelectionStrategyDecorator;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class SyncStatementTempTest {
    
    static class TemperatureLoggingESS extends AbstractEventSelectionStrategyDecorator<EventSelectionStrategy>{
        
        List<Boolean> isHotRecord = new ArrayList<>();
        
        public TemperatureLoggingESS(EventSelectionStrategy decorated) {
            super(decorated);
        }

        @Override
        public Set<BEvent> selectableEvents(Set<SyncStatement> statements, List<BEvent> externalEvents) {
            isHotRecord.add(statements.stream().filter(SyncStatement::isHot).findAny().isPresent());
            return decorated.selectableEvents(statements, externalEvents);
        }
        
    }
    
    @Test
    public void basicTempTest() throws InterruptedException {
        
        BProgram sut = new SingleResourceBProgram("statementtemp/basicTempTest.js");
        final BProgramRunner runner = new BProgramRunner(sut);
        
        TemperatureLoggingESS recorder = sut.setEventSelectionStrategy(
                new TemperatureLoggingESS(
                        new SimpleEventSelectionStrategy()));
        
        InMemoryEventLoggingListener listener = new InMemoryEventLoggingListener();
        runner.addListener(listener);
        runner.addListener( new PrintBProgramRunnerListener() );
        
        runner.run();
        
        assertEquals( recorder.isHotRecord, Arrays.asList(false, true, true, false));

    }
}
