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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.JsErrorViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;

import java.util.ArrayList;
import java.util.List;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BpLog;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import java.util.HashSet;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;

/**
 * Takes a {@link BProgram}, and verifies that it does not run into false
 * assertions or deadlock, given all possible event selections.
 * Take care to use the appropriate {@link VisitedStateStore} for the
 * {@link BProgram} being verified.
 * <p>
 * States are scanned using a DFS.
 *
 * @author michael
 */
public class DfsBProgramVerifier {

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    public final static long DEFAULT_MAX_TRACE = 1000;

    /**
     * Default number of iterations between invoking the progress listeners.
     */
    public final static long DEFAULT_ITERATION_COUNT_GAP = 1000;

    /**
     * A listener to the progress of the DFS state scanning.
     */
    public static interface ProgressListener {
        
        /**
         * Verifier {@code vfr} started a verification process.
         * @param vfr the verifier
         */
        void started(DfsBProgramVerifier vfr);

        /**
         * A periodical call to update progress.
         * @param count count of iterations.
         * @param statesHit count of states found.
         * @param vfr the verifier
         * @see DfsBProgramVerifier#setIterationCountGap(long) 
         */
        void iterationCount(long count, long statesHit, DfsBProgramVerifier vfr);
        
        /**
         * Verifier {@code vfr} hit the max trace length, and now pops its
         * iteration stack.
         * @param aTrace The trace the verifier examined when it hit the length limit.
         * @param vfr The verifier.
         * @see DfsBProgramVerifier#setMaxTraceLength(long) 
         */
        void maxTraceLengthHit(ExecutionTrace aTrace, DfsBProgramVerifier vfr);
        
        /**
         * Verifier {@code vfr} reports a found violation. It is up to the listener
         * to decide whether to continue the verification process and find more 
         * violations, or to terminate and return the current violation to the 
         * caller.
         * 
         * @param aViolation the violation found
         * @param vfr the verifier that found the violation
         * @return {@code true} for the verifier to continue the search, {@code false} otherwise.
         */
        boolean violationFound( Violation aViolation, DfsBProgramVerifier vfr );
        
        /**
         * The verifier {@code vfr} has finished the verification process.
         * @param vfr the verifier that found the violation
         */
        void done(DfsBProgramVerifier vfr);
    }
    
    private static class ViolatingPathFoundException extends Exception {
        final Violation v;

        public ViolatingPathFoundException(Violation v) {
            this.v = v;
        }
    }
    
    /**
     * A "null object" progress listener instance. Stops verification at the 
     * first violation found. Otherwise does nothing.
     */
    private static final ProgressListener NULL_PROGRESS_LISTENER = new ProgressListener() {
        @Override public void started(DfsBProgramVerifier vfr) {}
        @Override public void iterationCount(long count, long statesHit, DfsBProgramVerifier vfr) {}
        @Override public void maxTraceLengthHit(ExecutionTrace aTrace, DfsBProgramVerifier vfr) {}
        @Override public void done(DfsBProgramVerifier vfr) {}

        @Override
        public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
            return false;
        }
    };
        
    
    private long visitedEdgeCount;
    private VisitedStateStore visited = new BProgramSnapshotVisitedStateStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;
    private final List<DfsTraversalNode> currentPath = new ArrayList<>();
    private ProgressListener listener;
    private long iterationCountGap = DEFAULT_ITERATION_COUNT_GAP;
    private BProgram currentBProgram;
    private boolean debugMode = false;
    private final Set<ExecutionTraceInspection> inspections = new HashSet<>();
    private ArrayExecutionTrace trace;

    public DfsBProgramVerifier(BProgram aBp) {
        currentBProgram = aBp;
        trace = new ArrayExecutionTrace(currentBProgram);
    }

    public DfsBProgramVerifier() { }

    public VerificationResult verify(BProgram aBp) throws Exception {
        if ( listener == null ) {
            listener = NULL_PROGRESS_LISTENER;
        }
        currentBProgram = aBp;
        visitedEdgeCount = 0;
        currentPath.clear();
        visited.clear();
        trace = new ArrayExecutionTrace(currentBProgram);
        
        // in case no verifications were specified, use the defauls set.
        if ( inspections.isEmpty() ) { 
            inspections.addAll( ExecutionTraceInspections.DEFAULT_SET );
        }
        
        ExecutorService execSvc = BPjs.getExecutorServiceMaker().makeWithName("DfsBProgramRunner-" + INSTANCE_COUNTER.incrementAndGet());
        
        BpLog.LogLevel prevLevel = currentBProgram.getLogLevel();
        if ( ! BPjs.isLogDuringVerification() ) {
            currentBProgram.setLogLevel(BpLog.LogLevel.Off);
        }
        long start = System.currentTimeMillis();
        listener.started(this);
        try (Context cx = BPjs.enterRhinoContext() ) {
            Violation vio = dfsUsingStack(new DfsTraversalNode(currentBProgram, 
                currentBProgram.setup().start(execSvc, currentBProgram.getStorageModificationStrategy()), null), 
                execSvc
            );            
            
            long end = System.currentTimeMillis();
            return new VerificationResult(vio, end - start, visited.getVisitedStateCount(), visitedEdgeCount);

        } finally {            
            listener.done(this);
            execSvc.shutdown();
            if ( ! BPjs.isLogDuringVerification() ) {
                currentBProgram.setLogLevel(prevLevel);
            }
        }
    }

    protected Violation dfsUsingStack(DfsTraversalNode aStartNode, ExecutorService execSvc) throws Exception {
        long iterationCount = 0;

        push(aStartNode);
        Violation v = inspectCurrentTrace();
        if ( v != null ) return v;
                            
        while (!isPathEmpty()) {
            iterationCount++;
            
            if (debugMode) {
                printStatus(iterationCount, currentPath);
            }

            DfsTraversalNode curNode = peek();            

            if (pathLength() == maxTraceLength) {
                // fold stack;
                listener.maxTraceLengthHit(trace, this);
                pop();

            } else {
                try {
                    DfsTraversalNode nextNode = getUnvisitedNextNode(curNode, execSvc);
                    if (nextNode == null) {
                        // fold stack, retry next iteration;
                        DfsTraversalNode p = pop();
                        if ( p.getEventIterator().hasNext() ) {
                            throw new IllegalStateException("Still having some events to traverse: " + p.getEventIterator().next() );
                        }
                        
                    } else {
                        // go deeper 
                        push(nextNode);
                        v = inspectCurrentTrace();
                        if ( v != null ) return v;
                    }
                } catch (ViolatingPathFoundException vcfe ) {
                    return vcfe.v;
                }
            }
            
            if ( iterationCount % iterationCountGap == 0 ) {
                listener.iterationCount(iterationCount, visited.getVisitedStateCount(), this);
            }
        }

        return null;
    }

    protected DfsTraversalNode getUnvisitedNextNode(DfsTraversalNode src, ExecutorService execSvc) 
        throws ViolatingPathFoundException{
        while (src.getEventIterator().hasNext()) {
            final BEvent nextEvent = src.getEventIterator().next();
            try {
                DfsTraversalNode possibleNextNode = src.getNextNode(nextEvent, execSvc);
                visitedEdgeCount++;

                BProgramSyncSnapshot pns = possibleNextNode.getSystemState();
                int stateIndexOnTrace = trace.indexOf(pns);
                if ( stateIndexOnTrace > -1 ) {
                    // cycle found
                    trace.cycleTo(nextEvent, stateIndexOnTrace);
                    Set<Violation> res = inspections.stream().map(i->i.inspectTrace(trace))
                        .filter(o->o.isPresent()).map(Optional::get).collect(toSet());

                    for ( Violation v : res ) {
                        if ( ! listener.violationFound(v, this) ) {
                            throw new ViolatingPathFoundException(v);
                        }
                    }
                    
                } else if ( visited.isVisited(pns) ) {
                    // non cyclic, revisiting a state from a different path.
                    //     ... Quickly inspect the path and continue.
                    trace.advance(nextEvent, pns);
                    Set<Violation> res = inspections.stream().map(i->i.inspectTrace(trace))
                            .filter(o->o.isPresent()).map(Optional::get).collect(toSet());
                    if ( !res.isEmpty()  ) {
                        for ( Violation v : res ) {
                            if ( ! listener.violationFound(v, this) ) {
                                throw new ViolatingPathFoundException(v);
                            }
                        }
                    }
                    trace.pop();
                    
                } else {
                    // advance to this newly discovered node
                    return possibleNextNode;
                }
                
            } catch ( BPjsRuntimeException bprte ) {
                trace.advance(nextEvent, null);
                Violation jsev = new JsErrorViolation(trace, bprte);
                if ( ! listener.violationFound(jsev, this) ) {
                    throw new ViolatingPathFoundException(jsev);
                }
                trace.pop();
            }
        }
        return null;
    }

    public void setMaxTraceLength(long maxTraceLength) {
        this.maxTraceLength = maxTraceLength;
    }

    public long getMaxTraceLength() {
        return maxTraceLength;
    }

    public void setVisitedStateStore(VisitedStateStore aVisitedStateStore) {
        visited = aVisitedStateStore;
    }

    public VisitedStateStore getVisitedStateStore() {
        return visited;
    }

    public void setProgressListener(ProgressListener pl) {
        listener = (pl != null) ? pl : NULL_PROGRESS_LISTENER;
    }

    public void setIterationCountGap(long iterationCountGap) {
        this.iterationCountGap = iterationCountGap;
    }

    public long getIterationCountGap() {
        return iterationCountGap;
    }

    public BProgram getCurrentBProgram() {
        return currentBProgram;
    }

    void printStatus(long iteration, List<DfsTraversalNode> path) {
        System.out.println("Iteration " + iteration);
        System.out.println("  visited: " + visited.getVisitedStateCount());
        path.forEach(n -> System.out.println("  " + n.getLastEvent()));
    }
    
    private Violation inspectCurrentTrace() {
        Set<Violation> res = inspections.stream()
                                    .map(v->v.inspectTrace(trace))
                                    .filter(o->o.isPresent()).map(Optional::get)
                                    .collect(toSet());
        if ( res.size() > 0  ) {
            for ( Violation v : res ) {
                if ( ! listener.violationFound(v, this) ) {
                    return v;
                }
            }
            if (isDebugMode()) {
                System.out.println("-pop! (violation)-");
            }
            pop();
        }
        return null;
    }
    
    private void push(DfsTraversalNode n) {
        visited.store(n.getSystemState());
        currentPath.add(n);
        if (isDebugMode()) {
            System.out.println("-visiting: " + n);
        }
        if ( trace.getStateCount() == 0 ) {
            trace.push( n.getSystemState() );
        } else {
            trace.advance(n.getLastEvent(), n.getSystemState());
        }
    }

    private DfsTraversalNode pop() {
        DfsTraversalNode popped = currentPath.remove(currentPath.size() - 1);
        trace.pop();
        if (isDebugMode()) {
            System.out.println("-pop!-");
        }
        return popped;
    }

    private int pathLength() {
        return currentPath.size();
    }

    private boolean isPathEmpty() {
        return pathLength() == 0;
    }

    private DfsTraversalNode peek() {
        return isPathEmpty() ? null : currentPath.get(currentPath.size() - 1);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public <I extends ExecutionTraceInspection> I addInspection( I ins ) {
        inspections.add(ins);
        return ins;
    }
    
    public Set<ExecutionTraceInspection> getInspections() {
        return inspections;
    }
    
    public boolean removeInspection( ExecutionTraceInspection ins ) {
        return inspections.remove(ins);
    }

}
