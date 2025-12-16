/*
 * The MIT License
 *
 * Copyright 2017 BGU.
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
package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.bprogramio.log.BpLog;
import il.ac.bgu.cs.bp.bpjs.bprogramio.log.PrintStreamBpLog;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;

/**
 * A decorator that logs the state of the BProgram prior to letting the 
 * decorated {@link EventSelectionStrategy} select the event.Logging is done to a {@link PrintStream}, which defaults to {@code System.out}.
 *  
 * @param <ESS> The type of event selection strategy being decorated.
 * 
 * @author michael
 */
public class LoggingEventSelectionStrategyDecorator<ESS extends EventSelectionStrategy> extends AbstractEventSelectionStrategyDecorator<ESS> {
    
    private final BpLog out;
    
    public LoggingEventSelectionStrategyDecorator(ESS decorated, PrintWriter anOut) {
        super(decorated);
        out = new PrintStreamBpLog(anOut);
    }
    
    public LoggingEventSelectionStrategyDecorator(ESS decorated) {
        this(decorated, new PrintWriter(System.out));
    }

    @Override
    public Set<BEvent> selectableEvents(BProgramSyncSnapshot bpss) {
        final Set<BEvent> selectableEvents = getDecorated().selectableEvents(bpss);

        out.info("== Choosing Selectable Events ==");
        out.info("BThread Sync Statements:");
        bpss.getStatements().forEach( stmt -> {
            out.info("+ " + stmt.getBthread().getName() + ":" + (stmt.isHot()?" HOT":""));
            out.info("    Request: " + stmt.getRequest());
            out.info("    WaitFor: " + stmt.getWaitFor());
            out.info("    Block: "   + stmt.getBlock());
            out.info("    Interrupt: " + stmt.getInterrupt());
        });
        out.info("+ ExternalEvents: " + bpss.getExternalEvents());
        
        out.info("-- Selectable Events -----------");
        if ( selectableEvents.isEmpty() ){
            out.info(" - none -");
        } else {
            selectableEvents.stream().forEach( e -> out.info(" + " + e));
        }
        out.info("================================");

        return selectableEvents;
    }

    @Override
    public Optional<EventSelectionResult> select(BProgramSyncSnapshot bpss, Set<BEvent> selectableEvents) {
        Optional<EventSelectionResult> selectedEvent = getDecorated().select(bpss, selectableEvents);
        out.info("== Actual Event Selection ======");
        out.info( selectedEvent.toString() );
        out.info("================================");
        return selectedEvent;
    }
    
    
    
    
}
