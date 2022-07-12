package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotIO;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsException;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.ResumeBThread;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.StartBThread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.BPEngineTask;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.StartFork;
import il.ac.bgu.cs.bp.bpjs.internal.MapProxyConsolidator;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Conflict;
import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Success;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;

/**
 * The state of a {@link BProgram} when all its BThreads are at {@code bsync}.
 * This is more than a set of {@link BThreadSyncSnapshot}s, as it contains
 * the queue of external events as well.
 * 
 * @author michael
 */
public class BProgramSyncSnapshot {
    
    private static final AtomicInteger FORK_NEXT_ID = new AtomicInteger(0);
    
    private final BProgram bprog;
    private final Set<BThreadSyncSnapshot> threadSnapshots;
    private final Map<String, Object> dataStore;
    private final List<BEvent> externalEvents;
    private final AtomicReference<SafetyViolationTag> violationTag = new AtomicReference<>();
    private int hashCodeCache = Integer.MIN_VALUE;
    
    /** A flag to ensure the snapshot is only triggered once. */
    private boolean triggered=false;
    
    /**
     * A listener that populates the {@link #violationTag} field.
     */
    private static class ViolationRecorder implements BPEngineTask.Listener {
        private final BProgram bprogram;
        private final AtomicReference<SafetyViolationTag> vioRec;

        public ViolationRecorder(BProgram bprogram, AtomicReference<SafetyViolationTag> aViolationRecord) {
            this.bprogram = bprogram;
            vioRec = aViolationRecord;
        }
        
        @Override
        public void assertionFailed(FailedAssertionViolation fa) {
            vioRec.compareAndSet(null, fa);
        }

        @Override
        public void addFork(ForkStatement stmt) {
            bprogram.addFork(stmt);
        }
    }
    
    public BProgramSyncSnapshot(BProgram aBProgram, Set<BThreadSyncSnapshot> someThreadSnapshots,
                                 Map<String, Object> aDataStore,
                                List<BEvent> someExternalEvents, SafetyViolationTag aViolationRecord ) {
        threadSnapshots = someThreadSnapshots;
        dataStore = aDataStore;
        externalEvents = someExternalEvents;
        bprog = aBProgram;
        violationTag.set(aViolationRecord);
        
        threadSnapshots.forEach( ts -> ts.setBaseStore(dataStore) );
    }
    
    public BProgramSyncSnapshot copyWith( List<BEvent> updatedExternalEvents ) {
        return new BProgramSyncSnapshot(bprog, threadSnapshots, dataStore,
                                    updatedExternalEvents, violationTag.get());
    }

    /**
     * Starts the BProgram - runs all the registered b-threads to their first 
     * {@code bp.sync}. 
     * 
     * @param exSvc the executor service that will advance the threads.
     * @param sms The strategy to use when consolidating storage changes.
     * @return A snapshot of the program at the first {@code bp.sync}.
     * @throws java.lang.InterruptedException (since it's a blocking call)
     */
    public BProgramSyncSnapshot start( ExecutorService exSvc, StorageModificationStrategy sms ) throws InterruptedException {
        
        // execute b-threads
        Set<BThreadSyncSnapshot> nextRound = new HashSet<>(threadSnapshots.size());
        BPEngineTask.Listener halter = new ViolationRecorder(bprog, violationTag);
        nextRound.addAll(exSvc.invokeAll(threadSnapshots.stream()
                                .map(bt -> new StartBThread(this, bt, halter))
                                .collect(toList())
                ).stream().map(f -> safeGet(f) ).collect(toList())
        );
        executeAllAddedBThreads(nextRound, exSvc, halter);
        
        List<BEvent> nextExternalEvents = new ArrayList<>(getExternalEvents());
        nextExternalEvents.addAll( bprog.drainEnqueuedExternalEvents() );
                
        return createNextSnapshot(nextRound, nextExternalEvents, sms);
    }

    /**
     * Runs the program from the snapshot, triggering the passed event.
     * @param exSvc     The executor service that will advance the threads.
     * @param anEvent   The event selected.
     * @param listeners Will be informed in case of b-thread interrupts.
     * @param sms       A strategy for handling storage modifications.
     * @return A set of b-thread snapshots that should participate in the next cycle.
     * @throws InterruptedException (since it's a blocking call)
     */
    public BProgramSyncSnapshot triggerEvent(BEvent anEvent, ExecutorService exSvc, Iterable<BProgramRunnerListener> listeners, StorageModificationStrategy sms) throws InterruptedException, BPjsRuntimeException {
        if (anEvent == null) throw new IllegalArgumentException("Cannot trigger a null event.");
        if ( triggered ) {
            throw new IllegalStateException("A BProgramSyncSnapshot is not allowed to be triggered twice.");
    	}
    	triggered = true;
        listeners.forEach(l->l.eventSelected(bprog, anEvent));

        Set<BThreadSyncSnapshot> resumingThisRound = new HashSet<>(threadSnapshots.size());
        Set<BThreadSyncSnapshot> sleepingThisRound = new HashSet<>(threadSnapshots.size());
        List<BEvent> nextExternalEvents = new ArrayList<>(getExternalEvents());
        try (Context ctxt = BPjs.enterRhinoContext()) {
            handleInterrupts(anEvent, listeners, bprog, ctxt);
            nextExternalEvents.addAll(bprog.drainEnqueuedExternalEvents());
            
            // Split threads to those that advance this round and those that sleep.
            threadSnapshots.forEach( snapshot -> {
                (snapshot.getSyncStatement().shouldWakeFor(anEvent) ? resumingThisRound : sleepingThisRound).add(snapshot);
            });
        }
        
        BPEngineTask.Listener halter = new ViolationRecorder(bprog, violationTag);

        // add the run results of all those who advance this stage
        Set<BThreadSyncSnapshot> nextRound = new HashSet<>(threadSnapshots.size());
        try {
            nextRound.addAll(exSvc.invokeAll(
                                resumingThisRound.stream()
                                                 .map(bt -> new ResumeBThread(this, bt, anEvent, halter))
                                                 .collect(toList())
                    ).stream().map(f -> safeGet(f)).filter(Objects::nonNull).collect(toList())
            );

            // inform listeners which b-threads completed
            Set<String> nextRoundIds = nextRound.stream().map(t->t.getName()).collect(toSet());
            resumingThisRound.stream()
                    .filter(t->!nextRoundIds.contains(t.getName()))
                    .forEach(t->listeners.forEach(l->l.bthreadDone(bprog, t)));

            executeAllAddedBThreads(nextRound, exSvc, halter);
            
        } catch ( BPjsException re ) { 
            throw re;
            
        } catch ( RuntimeException re ) { 
            // try to peel the exception layers to get to the meaningful exception.
            Throwable cause = re;
            while ( cause instanceof RuntimeException  ) {
                if ( cause.getCause() != null ) {
                    cause = cause.getCause();
                } else {
                    throw (RuntimeException)cause;
                }
            }
            if ( cause instanceof ExecutionException ) {
                cause = cause.getCause();
            }
            if ( cause instanceof WrappedException ) {
                cause = cause.getCause();
            }
            
            if ( cause instanceof BPjsException ) {
                throw (BPjsException)cause;
            } else if ( cause instanceof EcmaError ) {
                throw new BPjsRuntimeException("JavaScript Error: " + cause.getMessage(), cause );
            } else throw re;
        }
        
        nextExternalEvents.addAll( bprog.drainEnqueuedExternalEvents() );

        // carry over BThreads that did not advance this round to next round.
        nextRound.addAll(sleepingThisRound);

        return createNextSnapshot(nextRound, nextExternalEvents, sms);
    }
    
    private BProgramSyncSnapshot createNextSnapshot(Set<BThreadSyncSnapshot> nextRound, List<BEvent> nextExternalEvents, StorageModificationStrategy sms) {
        // consolidate changes
        MapProxyConsolidator mpc = new MapProxyConsolidator();
        StorageConsolidationResult mpcRes = mpc.consolidate(nextRound);
        
        if ( mpcRes instanceof Conflict ) {
            mpcRes = sms.resolve((Conflict) mpcRes, this, nextRound);
        }
        
        // remove b-threads that are done, but but submitted some storage modifications.
        nextRound = nextRound.stream().filter(BThreadSyncSnapshot::canContinue).collect(toSet());
        
        if ( mpcRes instanceof Success ) {
            Success success = (Success)mpcRes;
            success = sms.incomingModifications(success, this, nextRound);
            Map<String, Object> updatedStore = success.apply(dataStore);
            nextRound.forEach( ts -> ts.clearStorageModifications() ); // changes were applied, so can be reset.
            return new BProgramSyncSnapshot(bprog, nextRound, updatedStore, nextExternalEvents, violationTag.get());
            
        } else {
            Conflict conflict = (Conflict) mpcRes;
            return new BProgramSyncSnapshot(bprog, nextRound, dataStore, nextExternalEvents, new StorageConflictViolation(conflict, "Storage conflict: " + conflict.toString()));
        }
    }
    
    private void handleInterrupts(BEvent anEvent, Iterable<BProgramRunnerListener> listeners, BProgram bprog, Context ctxt) {
        Set<BThreadSyncSnapshot> interrupted = threadSnapshots.stream()
                .filter(bt -> bt.getSyncStatement().getInterrupt().contains(anEvent))
                .collect(toSet());
        if (!interrupted.isEmpty()) {
            threadSnapshots.removeAll(interrupted);
            interrupted.forEach(bt -> {
                listeners.forEach(l -> l.bthreadRemoved(bprog, bt));
                bt.getInterrupt()
                        .ifPresent( func -> {
                            final Scriptable scope = bt.getScope();
                            try {
                                ctxt.callFunctionWithContinuations(func, scope, new Object[]{anEvent});
                            } catch ( BPjsRuntimeException e) {
                                listeners.forEach( l -> l.error(bprog, e));
                            } catch ( WrappedException wpe ) {
                                if ( wpe.getWrappedException() instanceof BPjsRuntimeException ) {
                                    BPjsRuntimeException e = (BPjsRuntimeException) wpe.getWrappedException();
                                    listeners.forEach( l -> l.error(bprog, e));
                                }
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
    
    public Set<SyncStatement> getStatements() {
        return getBThreadSnapshots().stream().map(BThreadSyncSnapshot::getSyncStatement)
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
    
    /**
     * If the b-program has violated some requirement while getting to {@code this}
     * state, it is considered to be in <b>invalid</b> state. This happens when
     * a b-thread makes an failed assertion.
     * 
     * @return {@code true} iff the program is in valid state.
     * @see #getViolationTag() 
     */
    public boolean isStateValid() {
        return violationTag.get() == null;
    }
    
    public SafetyViolationTag getViolationTag() {
        return violationTag.get();
    }

    public Map<String, Object> getDataStore() {
        return dataStore;
    }
    
    /**
     * Returns {@code true} if any of the b-threads at this point is at a "hot"
     * sync. Similar to the "hot cut" idiom in LSC.
     * @return {@code true} is at least one of the b-threads is at a hot sync; {@code false} otherwise.
     */
    public boolean isHot() {
        return getBThreadSnapshots().stream()
                    .map(BThreadSyncSnapshot::getSyncStatement)
                    .filter(SyncStatement::isHot)
                    .findAny().isPresent();
    }
    
    private BThreadSyncSnapshot safeGet(Future<BThreadSyncSnapshot> fbss) {
        try {
            return fbss.get();
        } catch (InterruptedException | ExecutionException | WrappedException ex) {
            if ( ex.getCause() instanceof BPjsException ) {
                throw (BPjsException)ex.getCause();
            } else {
                throw new RuntimeException("Error running a b-thread: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Executes and adds all newly registered b-threads, until no more new b-threads 
     * are registered.
     * @param nextRound the set of b-threads that will participate in the next round
     * @param exSvc The executor service to run the b-threads
     * @param listener handling assertion failures, if they happen.
     * @throws InterruptedException 
     */
    private void executeAllAddedBThreads(Set<BThreadSyncSnapshot> nextRound, ExecutorService exSvc, BPEngineTask.Listener listener) throws InterruptedException {
        // if any new bthreads are added, run and add their result
        Set<BThreadSyncSnapshot> addedBThreads = bprog.drainRecentlyRegisteredBthreads();
        Set<ForkStatement> addedForks = bprog.drainRecentlyAddedForks();
        while ( ((!addedBThreads.isEmpty()) || (!addedForks.isEmpty())) 
                && !exSvc.isShutdown() ) {
            Stream<BPEngineTask> threadStream = addedBThreads.stream()
                .map(bt -> new StartBThread(this, bt, listener));
            Stream<BPEngineTask> forkStream = addedForks.stream().flatMap( f -> convertToTasks(f, listener) );
            
            nextRound.addAll(exSvc.invokeAll(Stream.concat(forkStream, threadStream).collect(toList())).stream()
                     .map(f -> safeGet(f) ).filter(Objects::nonNull).collect(toList()));
            addedBThreads = bprog.drainRecentlyRegisteredBthreads();
            addedForks = bprog.drainRecentlyAddedForks();
        }
    }
    
    Stream<StartFork> convertToTasks(ForkStatement fkStmt, BPEngineTask.Listener listener) {
        
        // construct a BThreadSyncSnapshot
        BThreadSyncSnapshot btss = fkStmt.makeSnapshot(
            "f" + FORK_NEXT_ID.incrementAndGet() + "$" + fkStmt.getForkingBThread().getName(),
            this
        );
        
        // duplicate snapshot and register the copy with the b-program
        try (Context cx = BPjs.enterRhinoContext()) {
            BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(bprog);
            BThreadSyncSnapshot forkedBT = io.deserializeBThread(io.serializeBThread(btss), dataStore);
            bprog.registerForkedChild(btss);
            
            // on child forks, the fork() statement returns 1.
            return Stream.of(new StartFork(1, this, forkedBT, listener));
        }
    }
    
    @Override
    public int hashCode() {
        if ( hashCodeCache == Integer.MIN_VALUE  ) {
            hashCodeCache = 37*(threadSnapshots.hashCode() + 
                            externalEvents.hashCode() + 
                            ScriptableUtils.jsHashCode(dataStore)+1);
        }
        return hashCodeCache;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BProgramSyncSnapshot other = (BProgramSyncSnapshot) obj;
        
        // optimization: non-equality by hash code - if we have one.
        if ( hashCodeCache != Integer.MIN_VALUE && other.hashCodeCache != Integer.MIN_VALUE ) {
            if ( hashCodeCache != other.hashCodeCache ) return false;
        }
        
        if (isStateValid() != other.isStateValid()) {
            return false;
        }
        if (!isStateValid()) {
            if (!getViolationTag().equals(other.getViolationTag()) ) {
                return false;
            }
        }
        
        if ( ! ScriptableUtils.jsMapEquals(getDataStore(), other.getDataStore()) ) return false;
        if ( ! getExternalEvents().equals(other.getExternalEvents()) ) return false;
        
        return Objects.equals(threadSnapshots, other.threadSnapshots);
    }
}
