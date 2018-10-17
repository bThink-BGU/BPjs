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

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import java.util.ArrayList;
import static java.util.Collections.reverseOrder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs a {@link BProgram} to completion. Uses an {@link EventSelectionStrategy}
 * to select which event to choose at each point.
 * 
 * @author michael
 */
public class BProgramRunner implements Runnable {
    
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
    private final int instanceNum = INSTANCE_COUNTER.incrementAndGet();
    private ExecutorService execSvc = null;
    private BProgram bprog = null;
    private final List<BProgramRunnerListener> listeners = new ArrayList<>();
    private FailedAssertion failedAssertion;
    private final AtomicBoolean go = new AtomicBoolean(true);
    private volatile boolean halted;
    
    
    public BProgramRunner(){
        this(null);
    }
    public BProgramRunner(BProgram aBProgram) {
        bprog = aBProgram;
        if ( bprog!=null ) {
            bprog.setAddBThreadCallback( (bp,bt)->listeners.forEach(l->l.bthreadAdded(bp, bt)));
        }
    }
    
    @Override
    public void run() {
        try {
            // setup bprogram and runtime parts.
            execSvc = ExecutorServiceMaker.makeWithName("BProgramRunner-" + instanceNum );
            failedAssertion = null;
            listeners.forEach(l -> l.starting(bprog));
            BProgramSyncSnapshot cur = bprog.setup();
            cur.getBThreadSnapshots().forEach(sn->listeners.forEach( l -> l.bthreadAdded(bprog, sn)) );

            // start it
            listeners.forEach(l -> l.started(bprog));
            cur = cur.start(execSvc);

            go.set(true);
            halted = false;

            if ( ! cur.isStateValid() ) {
                failedAssertion = cur.getFailedAssertion();
                listeners.forEach( l->l.assertionFailed(bprog, failedAssertion));
                go.set(false);
            }

            // while snapshot not empty, select an event and get the next snapshot.
            while ( (!cur.noBThreadsLeft()) && go.get() ) {

                // see which events are selectable
                Set<BEvent> possibleEvents = bprog.getEventSelectionStrategy().selectableEvents(cur.getStatements(), cur.getExternalEvents());
                if ( possibleEvents.isEmpty() ) {
                    // Superstep done: No events available or selection.
                    
                    if ( bprog.isWaitForExternalEvents() ) {
                        listeners.forEach( l->l.superstepDone(bprog) );
                        BEvent next = bprog.takeExternalEvent(); // and now we wait for external event
                        if ( next == null ) {
                            go.set(false); // program no longer waits for external events
                        } else {
                            cur.getExternalEvents().add(next);
                        }

                    } else {
                        // Ending the program - no selectable event.
                        listeners.forEach(l->l.superstepDone(bprog));
                        go.set(false); 
                    }

                } else {
                    // we can select some events - select one and advance.
                    Optional<EventSelectionResult> res = bprog.getEventSelectionStrategy().select(cur.getStatements(), cur.getExternalEvents(), possibleEvents);

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
                        cur = cur.triggerEvent(esr.getEvent(), execSvc, listeners);
                        if ( ! cur.isStateValid() ) {
                            failedAssertion = cur.getFailedAssertion();
                            listeners.forEach( l->l.assertionFailed(bprog, failedAssertion));
                            go.set(false);
                        }

                    } else {
                        // edge case: we can select events, but we didn't. Might be a bug in the EventSelectionStrategy.
                        go.set(false); 
                    }
                }
            }
            if ( halted ) {
                listeners.forEach(l->l.halted(bprog)); 
            } else {
                listeners.forEach(l->l.ended(bprog));
            }
            
        } catch (InterruptedException itr) {
            System.err.println("BProgramRunner interrupted: " + itr.getMessage() );
            
        } finally {
            execSvc.shutdown();
        }
    }
    
    /**
     * Halts the running b-program. The program will terminate on the next
     * synchronization point.
     */
    public void halt() {
        halted=true;
        go.set(false);
    }
    
    /**
     * @return {@code true} iff the program has terminated because of 
     * a failed assertion, {@code false} otherwise. 
     */
    public boolean hasFailedAssertion() {
        return failedAssertion!=null;
    }

    public FailedAssertion getFailedAssertion() {
        return failedAssertion;
    }
    
    public BProgram getBProgram() {
        return bprog;
    }

    public void setBProgram(BProgram aBProgram) {
        bprog = aBProgram;
        if ( bprog!=null ) {
            bprog.setAddBThreadCallback( (bp,bt)->listeners.forEach(l->l.bthreadAdded(bp, bt)));
        }
    }
    
    /**
     * Adds a listener to the BProgram.
     * @param <R> Actual type of listener.
     * @param aListener the listener to add.
     * @return The added listener, to allow call chaining.
     */
    public <R extends BProgramRunnerListener> R addListener(R aListener) {
        listeners.add(aListener);
        return aListener;
    }

    /**
     * Removes the listener from the program. If the listener is not registered,
     * this call is ignored. In other words, this call is idempotent.
     * @param aListener the listener to remove.
     */
    public void removeListener(BProgramRunnerListener aListener) {
        listeners.remove(aListener);
    }

}
