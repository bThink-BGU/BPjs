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
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.BProgramListener;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.eventselection.SimpleEventSelectionStrategy;
import java.util.ArrayList;
import static java.util.Collections.reverseOrder;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Runs a {@link BProgram} to completion. 
 * 
 * @author michael
 */
public class BProgramRunner {
    
    private BProgram bprog = null;
    private EventSelectionStrategy eventSelectionStrategy;
    private final List<BProgramListener> listeners = new ArrayList<>();
    
    public BProgramRunner(){
        this(null);
    }
    public BProgramRunner(BProgram aBProgram) {
        this( aBProgram, new SimpleEventSelectionStrategy() );
    }
    
    public BProgramRunner(BProgram aBProgram, EventSelectionStrategy ess) {
        bprog = aBProgram;
        eventSelectionStrategy = ess;
    }
    
    public void start() throws InterruptedException {
        // setup bprogram
        listeners.forEach(l -> l.starting(bprog));
        BProgramSyncSnapshot cur = bprog.setup();
        
        cur.getBThreadSnapshots().forEach(sn->listeners.forEach( l -> l.bthreadAdded(bprog, sn)) );
        
        // start it
        listeners.forEach(l -> l.started(bprog));
        cur = cur.start(bprog, listeners);
        
        // while snapshot not empty, select an event and get the next snapshot.
        boolean go=true;
        while ( (!cur.isEmpty()) && go ) {
            if (cur.getExternalEvents().remove(BProgram.NO_MORE_DAEMON)) {
                bprog.setDaemonMode(false);
            }
            Set<BEvent> possibleEvents = eventSelectionStrategy.selectableEvents(cur.getStatements(), cur.getExternalEvents());
            if ( possibleEvents.isEmpty() ) {
                if ( bprog.isDaemonMode() ) {
                    listeners.forEach( l->l.superstepDone(bprog) );
                    BEvent next = bprog.takeExternalEvent(); // and now we wait.
                    if ( next == null ) {
                        go=false; // program is not a daemon anymore.
                    } else {
                        cur.getExternalEvents().add(next);
                    }
                    
                } else {
                    // Ending the program - no selectable event.
                    listeners.forEach(l->l.superstepDone(bprog));
                    go = false;
                }
            } else {
                Optional<EventSelectionResult> res = eventSelectionStrategy.select(cur.getStatements(), cur.getExternalEvents(), possibleEvents);

                if ( res.isPresent() ) {
                    EventSelectionResult esr = res.get();
                    
                    if ( ! esr.getIndicesToRemove().isEmpty() ) {
                        // the event selection affcted the external event queue.
                        List<BEvent> updatedExternals = new ArrayList<>(cur.getExternalEvents());
                        esr.getIndicesToRemove().stream().sorted(reverseOrder())
                            .forEach( idxObj -> updatedExternals.remove(idxObj.intValue()) );
                        cur = cur.copyWith(updatedExternals);
                    }
                    
                    listeners.forEach(l->l.eventSelected(bprog, esr.getEvent())); 
                    cur = cur.triggerEvent(esr.getEvent(), bprog, listeners);
                    
                } else {
                    go = false;
                }
            }
        }
        listeners.forEach(l->l.ended(bprog)); 
    }

    public BProgram getBProgram() {
        return bprog;
    }

    public void setBProgram(BProgram aBProgram) {
        this.bprog = aBProgram;
    }
    
    /**
     * Adds a listener to the BProgram.
     * @param <R> Actual type of listener.
     * @param aListener the listener to add.
     * @return The added listener, to allow call chaining.
     */
    public <R extends BProgramListener> R addListener(R aListener) {
        listeners.add(aListener);
        return aListener;
    }

    /**
     * Removes the listener from the program. If the listener is not registered,
     * this call is ignored. In other words, this call is idempotent.
     * @param aListener the listener to remove.
     */
    public void removeListener(BProgramListener aListener) {
        listeners.remove(aListener);
    }

    public EventSelectionStrategy getEventSelectionStrategy() {
        return eventSelectionStrategy;
    }

    public void setEventSelectionStrategy(EventSelectionStrategy eventSelectionStrategy) {
        this.eventSelectionStrategy = eventSelectionStrategy;
    }
    
}
