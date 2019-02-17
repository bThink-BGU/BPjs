package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotIO;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BThreadSyncSnapshotInputStream;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StreamObjectStub;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StubProvider;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.ResumeBThread;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.StartBThread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Scriptable;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.BPEngineTask;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.StartFork;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.mozilla.javascript.ScriptableObject;

/**
 * The state of a {@link BProgram} when all its BThreads are at {@code bsync}.
 * This is more than a set of {@link BThreadSyncSnapshot}s, as it contains
 * the queue of external events as well.
 * 
 * @author michael
 */
public class BProgramSyncSnapshot {
    
    private static final AtomicInteger FORK_NEXT_ID = new AtomicInteger(0);
    
    private final Set<BThreadSyncSnapshot> threadSnapshots;
    private final List<BEvent> externalEvents;
    private final BProgram bprog;
    private final AtomicReference<FailedAssertion> violationRecord = new AtomicReference<>();
    
    /** A flag to ensure the snapshot is only triggered once. */
    private boolean triggered=false;
    
    /**
     * A listener that populates the {@link #violationRecord} field.
     */
    private static class ViolationRecorder implements BPEngineTask.Listener {
        private final BProgram bprogram;
        private final AtomicReference<FailedAssertion> vioRec;

        public ViolationRecorder(BProgram bprogram, AtomicReference<FailedAssertion> aViolationRecord) {
            this.bprogram = bprogram;
            vioRec = aViolationRecord;
        }
        
        @Override
        public void assertionFailed(FailedAssertion fa) {
            vioRec.compareAndSet(null, fa);
        }

        @Override
        public void addFork(ForkStatement stmt) {
            bprogram.addFork(stmt);
        }
    }
    
    public BProgramSyncSnapshot(BProgram aBProgram, Set<BThreadSyncSnapshot> someThreadSnapshots,
                                List<BEvent> someExternalEvents, FailedAssertion aViolationRecord ) {
        threadSnapshots = someThreadSnapshots;
        externalEvents = someExternalEvents;
        bprog = aBProgram;
        violationRecord.set(aViolationRecord);
    }
    
    public BProgramSyncSnapshot copyWith( List<BEvent> updatedExternalEvents ) {
        return new BProgramSyncSnapshot(bprog, threadSnapshots, updatedExternalEvents, violationRecord.get());
    }

    /**
     * Starts the BProgram - runs all the registered b-threads to their first 
     * {@code bsync}. 
     * 
     * @param exSvc the executor service that will advance the threads.
     * @return A snapshot of the program at the first {@code bsync}.
     * @throws java.lang.InterruptedException (since it's a blocking call)
     */
    public BProgramSyncSnapshot start( ExecutorService exSvc ) throws InterruptedException {
        Set<BThreadSyncSnapshot> nextRound = new HashSet<>(threadSnapshots.size());
        BPEngineTask.Listener halter = new ViolationRecorder(bprog, violationRecord);
        nextRound.addAll(exSvc.invokeAll(threadSnapshots.stream()
                                .map(bt -> new StartBThread(bt, halter))
                                .collect(toList())
                ).stream().map(f -> safeGet(f) ).collect(toList())
        );
        executeAllAddedBThreads(nextRound, exSvc, halter);
        List<BEvent> nextExternalEvents = new ArrayList<>(getExternalEvents());
        nextExternalEvents.addAll( bprog.drainEnqueuedExternalEvents() );
        return new BProgramSyncSnapshot(bprog, nextRound, nextExternalEvents, violationRecord.get());
    }

    /**
     * Runs the program from the snapshot, triggering the passed event.
     * @param exSvc the executor service that will advance the threads.
     * @param anEvent the event selected.
     * @param listeners will be informed in case of b-thread interrupts
     * @return A set of b-thread snapshots that should participate in the next cycle.
     * @throws InterruptedException (since it's a blocking call)
     */
    public BProgramSyncSnapshot triggerEvent(BEvent anEvent, ExecutorService exSvc, Iterable<BProgramRunnerListener> listeners) throws InterruptedException {
        if (anEvent == null) throw new IllegalArgumentException("Cannot trigger a null event.");
        if ( triggered ) {
            throw new IllegalStateException("A BProgramSyncSnapshot is not allowed to be triggered twice.");
    	}
    	triggered = true;
        
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
                (snapshot.getSyncStatement().shouldWakeFor(anEvent) ? resumingThisRound : sleepingThisRound).add(snapshot);
            });
        } finally {
            Context.exit();
        }
        
        BPEngineTask.Listener halter = new ViolationRecorder(bprog, violationRecord);
        
        try {
            // add the run results of all those who advance this stage
            nextRound.addAll(exSvc.invokeAll(
                                resumingThisRound.stream()
                                                 .map(bt -> new ResumeBThread(bt, anEvent, halter))
                                                 .collect(toList())
                    ).stream().map(f -> safeGet(f)).filter(Objects::nonNull).collect(toList())
            );

            // inform listeners which b-threads completed
            Set<String> nextRoundIds = nextRound.stream().map(t->t.getName()).collect(toSet());
            resumingThisRound.stream()
                    .filter(t->!nextRoundIds.contains(t.getName()))
                    .forEach(t->listeners.forEach(l->l.bthreadDone(bprog, t)));

            executeAllAddedBThreads(nextRound, exSvc, halter);
            nextExternalEvents.addAll( bprog.drainEnqueuedExternalEvents() );

            // carry over BThreads that did not advance this round to next round.
            nextRound.addAll(sleepingThisRound);


            return new BProgramSyncSnapshot(bprog, nextRound, nextExternalEvents, violationRecord.get());
        
        } catch ( RejectedExecutionException ree ) {
            // The executor thread pool must have been shut down, e.g. due to program violation.
            return new BProgramSyncSnapshot(bprog, Collections.emptySet(), nextExternalEvents, violationRecord.get());
            
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
                            } catch ( ContinuationPending ise ) {
                                throw new BPjsRuntimeException("Cannot call bsync or fork from a break-upon handler. Please consider pushing an external event.");
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
     * @see #getFailedAssertion() 
     */
    public boolean isStateValid() {
        return violationRecord.get() == null;
    }
    
    public FailedAssertion getFailedAssertion() {
        return violationRecord.get();
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
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(BProgramSyncSnapshot.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Error running a bthread: " + ex.getMessage(), ex);
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
                .map(bt -> new StartBThread(bt, listener));
            Stream<BPEngineTask> forkStream = addedForks.stream().flatMap( f -> convertToTasks(f, listener) );
            
            nextRound.addAll(exSvc.invokeAll(Stream.concat(forkStream, threadStream).collect(toList())).stream()
                     .map(f -> safeGet(f) ).filter(Objects::nonNull).collect(toList()));
            addedBThreads = bprog.drainRecentlyRegisteredBthreads();
            addedForks = bprog.drainRecentlyAddedForks();
        }
    }
    
    Stream<StartFork> convertToTasks(ForkStatement fkStmt, BPEngineTask.Listener listener) {
        
        // read continuation
        Object cont=null;
        final BProgramJsProxy bpProxy = new BProgramJsProxy(bprog);

        StubProvider stubPrv = (StreamObjectStub stub) -> {
            if (stub == StreamObjectStub.BP_PROXY) {
                return bpProxy;
            }
            throw new IllegalArgumentException("Unknown stub " + stub);
        };

        try ( ByteArrayInputStream bais = new ByteArrayInputStream(fkStmt.getSerializedContinuation());
            BThreadSyncSnapshotInputStream bssis = new BThreadSyncSnapshotInputStream(bais,
                                                                ScriptableObject.getTopLevelScope(bprog.getGlobalScope()), stubPrv) ) {
            cont = bssis.readObject();
        } catch (ClassNotFoundException|IOException ex) {
            throw new RuntimeException("Error while deserializing fork continuation: " + ex.getMessage(), ex);
        }
        
        // construct a BThreadSyncSnapshot
        BThreadSyncSnapshot btss = new BThreadSyncSnapshot(
            "f" + FORK_NEXT_ID.incrementAndGet() + "$" + fkStmt.getForkingBThread().getName(),
            fkStmt.getForkingBThread().getEntryPoint(), 
            fkStmt.getForkingBThread().getInterrupt().orElse(null),
            cont,
            null
        );
        
        // duplicate snapshot and register the copy with the b-program
        try {
            Context.enter();
            BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(bprog);
            BThreadSyncSnapshot forkedBT = io.deserializeBThread(io.serializeBThread(btss));
            bprog.registerForkedChild(btss);
            return Stream.of(new StartFork(fkStmt, forkedBT, listener, bprog));
        } finally {
            Context.exit();
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash( threadSnapshots, externalEvents );
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
        if (isStateValid() != other.isStateValid()) {
            return false;
        }
        if (!isStateValid()) {
            if (!getFailedAssertion().equals(other.getFailedAssertion()) ) {
                return false;
            }
        }
        if ( ! getExternalEvents().equals(other.getExternalEvents()) ) {
            return false;
        }
        return Objects.equals(threadSnapshots, other.threadSnapshots);
    }
}
