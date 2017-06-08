package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.exceptions.BProgramException;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.BProgramListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.tasks.ResumeBThread;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.tasks.StartBThread;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.search.BProgramSyncSnapshotIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Scriptable;

/**
 * The state of a {@link BProgram} when all its BThreads are at {@code bsync}.
 * This is more than a set of {@link BThreadSyncSnapshot}s, as it contains
 * the queue of external events as well.
 * 
 * @author michael
 */
public class BProgramSyncSnapshot {
    
    private static final ThreadLocal<ExecutorService> EXECUTORS = new ThreadLocal<ExecutorService>(){
        @Override
        protected ExecutorService initialValue() {
            return new ForkJoinPool();
        }
    };
    
    
    private final Set<BThreadSyncSnapshot> threadSnapshots;
    private final List<BEvent> externalEvents;
    private final BProgram bprog;

	public boolean triggered=false;
    
    public BProgramSyncSnapshot(BProgram aBProgram, Set<BThreadSyncSnapshot> threadSnapshots, List<BEvent> externalEvents) {
        this.threadSnapshots = threadSnapshots;
        this.externalEvents = externalEvents;
        bprog = aBProgram;
    }
    
    public BProgramSyncSnapshot copyWith( List<BEvent> updatedExternalEvents ) {
        return new BProgramSyncSnapshot(bprog, threadSnapshots, updatedExternalEvents);
    }

    /**
     * Starts the BProgram - runs all the registered b-threads to their first 
     * {@code bsync}. 
     * 
     * @return A snapshot of the program at the first {@code bsync}.
     * @throws java.lang.InterruptedException
     */
    public BProgramSyncSnapshot start() throws InterruptedException {
        Set<BThreadSyncSnapshot> nextRound = new HashSet<>(threadSnapshots.size());
        nextRound.addAll(EXECUTORS.get().invokeAll(threadSnapshots.stream()
                    .map(bt -> new StartBThread(bt))
                    .collect(toList())
                ).stream().map(f -> safeGet(f) ).collect(toList())
        );
        
        executeAllAddedBThreads(nextRound);
        List<BEvent> nextExternalEvents = new ArrayList<>(getExternalEvents());
        nextExternalEvents.addAll( bprog.drainEnqueuedExternalEvents() );
        return new BProgramSyncSnapshot(bprog, nextRound, nextExternalEvents);
    }

    public BProgramSyncSnapshot triggerEvent(BEvent anEvent) throws InterruptedException {
    	if(triggered) {
    		throw new IllegalStateException("A BProgramSyncSnapshot is not allowed to be triggered twice.");
    	}
    	
    	triggered = true;
    	
        return triggerEvent(anEvent, Collections.emptySet());
    }
    
    /**
     * Runs the program from the snapshot, triggering the passed event.
     * @param anEvent the event selected.
     * @param listeners 
     * @return A set of b-thread snapshots that should participate in the next cycle.
     * @throws InterruptedException 
     */
    public BProgramSyncSnapshot triggerEvent(BEvent anEvent, Iterable<BProgramListener> listeners) throws InterruptedException {
        if (anEvent == null) throw new IllegalArgumentException("Cannot trigger a null event.");
        
        Set<BThreadSyncSnapshot> resumingThisRound = new HashSet<>(threadSnapshots.size());
        Set<BThreadSyncSnapshot> sleepingThisRound = new HashSet<>(threadSnapshots.size());
        Set<BThreadSyncSnapshot> nextRound = new HashSet<>(threadSnapshots.size());
        List<BEvent> nextExternalEvents = new ArrayList<>(getExternalEvents());
        try {
            Context ctxt = Context.enter();
            handleInterrupts(anEvent, listeners, bprog, ctxt);
            nextExternalEvents.addAll(bprog.drainEnqueuedExternalEvents());
            
            // Split threads to those that advance this round and those that sleep.
            threadSnapshots.forEach( snapshot -> {
                (snapshot.getBSyncStatement().shouldWakeFor(anEvent) ? resumingThisRound : sleepingThisRound).add(snapshot);
            });
        } finally {
            Context.exit();
        }
        
        // add the run results of all those who advance this stage
        nextRound.addAll(EXECUTORS.get().invokeAll(
                            resumingThisRound.stream()
                                             .map(bt -> new ResumeBThread(bt, anEvent))
                                             .collect(toList())
                ).stream().map(f -> safeGet(f) ).filter(Objects::nonNull).collect(toList())
        );
        
        // inform listeners which b-threads completed
        Set<String> nextRoundIds = nextRound.stream().map(t->t.getName()).collect(toSet());
        resumingThisRound.stream().filter(t->!nextRoundIds.contains(t.getName()))
                .forEach(t->listeners.forEach(l->l.bthreadDone(bprog, t)));
        
        executeAllAddedBThreads(nextRound);
        nextExternalEvents.addAll( bprog.drainEnqueuedExternalEvents() );
        
        // carry over BThreads that did not advance this round to next round.
        nextRound.addAll(sleepingThisRound);
        
        return new BProgramSyncSnapshot(bprog, nextRound, nextExternalEvents);
    }

    private void handleInterrupts(BEvent anEvent, Iterable<BProgramListener> listeners, BProgram bprog, Context ctxt) {
        Set<BThreadSyncSnapshot> interrupted = threadSnapshots.stream()
                .filter(bt -> bt.getBSyncStatement().getInterrupt().contains(anEvent))
                .collect(toSet());
        if (!interrupted.isEmpty()) {
            threadSnapshots.removeAll(interrupted);
            interrupted.forEach(bt -> {
                listeners.forEach(l -> l.bthreadRemoved(bprog, bt));
                bt.getInterrupt()
                        .ifPresent( func -> {
                            final Scriptable scope = bt.getScope();
                            scope.delete("bsync"); // can't call bsync from a break handler.
                            try {
                                ctxt.callFunctionWithContinuations(func, scope, new Object[]{anEvent});
                            } catch ( ContinuationPending ise ) {
                                throw new BProgramException("Cannot call bsync from a break-upon handler. Consider pushing an external event.");
                            }
                        });
            });
        }
    }

    public List<BEvent> getExternalEvents() {
        return externalEvents;
    }

    public Set<BThreadSyncSnapshot> getBThreadSnapshots() {
        return threadSnapshots;
    }
    
    public Set<BSyncStatement> getStatements() {
        return getBThreadSnapshots().stream().map(BThreadSyncSnapshot::getBSyncStatement)
                .collect(toSet());
    }
    
    /**
     * Does this snapshot have any b-threads to run? If not, this means that
     * the b-program has terminated.
     * 
     * @return {@code true} iff the snapshot contains b-threads.
     */
    public boolean noBThreadsLeft() {
        return threadSnapshots.isEmpty();
    }
    
    /**
     * 
     * @return The BProgram this object is a snapshot of.
     */
    public BProgram getBProgram() {
        return bprog;
    }
    
    private BThreadSyncSnapshot safeGet(Future<BThreadSyncSnapshot> fbss) {
        try {
            return fbss.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(BProgramSyncSnapshot.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Error running a bthread: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Executes and adds all newly registered b-threads, until no more new b-threads 
     * are registered.
     * @param listeners
     * @param nextRound the set of b-threads that will participate in the next round
     * @throws InterruptedException 
     */
    private void executeAllAddedBThreads(Set<BThreadSyncSnapshot> nextRound) throws InterruptedException {
        // if any new bthreads are added, run and add their result
        Set<BThreadSyncSnapshot> added = bprog.drainRecentlyRegisteredBthreads();
        while ( ! added.isEmpty() ) {
            nextRound.addAll(EXECUTORS.get().invokeAll(
                    added.stream()
                            .map(bt -> new StartBThread(bt))
                            .collect(toList())
            ).stream().map(f -> safeGet(f) ).filter(Objects::nonNull).collect(toList()));
            added = bprog.drainRecentlyRegisteredBthreads();
        }
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(getBProgram());
		return io.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(((BProgramSyncSnapshot)obj).getBProgram());
		return io.equals(getBProgram());
	}

}
