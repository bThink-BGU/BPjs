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

import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import java.util.ArrayList;
import java.util.List;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import java.util.HashSet;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.stream.Collectors.toSet;

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
        void maxTraceLengthHit(List<DfsTraversalNode> aTrace, DfsBProgramVerifier vfr);
        
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
         * @param vfr 
         */
        void done(DfsBProgramVerifier vfr);
    }
    
    private static class ViolatingCycleFoundException extends Exception{
        final Violation v;

        public ViolatingCycleFoundException(Violation v) {
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
        @Override public void maxTraceLengthHit(List<DfsTraversalNode> aTrace, DfsBProgramVerifier vfr) {}
        @Override public void done(DfsBProgramVerifier vfr) {}

        @Override
        public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
            return false;
        }
    };
        
    
    private long visitedEdgeCount;
    private VisitedStateStore visited = new BThreadSnapshotVisitedStateStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;
    private final List<DfsTraversalNode> currentPath = new ArrayList<>();
    private ProgressListener listener;
    private long iterationCountGap = DEFAULT_ITERATION_COUNT_GAP;
    private BProgram currentBProgram;
    private boolean debugMode = false;
    private final Set<ExecutionTraceInspection> inspections = new HashSet<>();
    private ArrayExecutionTrace trace;

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
        
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("DfsBProgramRunner-" + INSTANCE_COUNTER.incrementAndGet());
        long start = System.currentTimeMillis();
        listener.started(this);
        Violation vio = dfsUsingStack(new DfsTraversalNode(currentBProgram, currentBProgram.setup().start(execSvc), null), execSvc);
        long end = System.currentTimeMillis();
        execSvc.shutdown();
        listener.done(this);
        return new VerificationResult(vio, end - start, visited.getVisitedStateCount(), visitedEdgeCount);
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
                listener.maxTraceLengthHit(currentPath, this);
                pop();

            } else {
                try {
                    DfsTraversalNode nextNode = getUnvisitedNextNode(curNode, execSvc);
                    if (nextNode == null) {
                        // fold stack, retry next iteration;
                        if (isDebugMode()) {
                            System.out.println("-pop!-");
                        }
                        pop();
                    } else {
                        // go deeper 
                        if (isDebugMode()) {
                            System.out.println("-visiting: " + nextNode);
                        }
                        push(nextNode);
                        v = inspectCurrentTrace();
                        if ( v != null ) return v;
                    }
                } catch (ViolatingCycleFoundException vcfe ) {
                    return vcfe.v;
                }
            }
            
            if ( iterationCount % iterationCountGap == 0 ) {
                listener.iterationCount(iterationCount, visited.getVisitedStateCount(), this);
            }
        }

        return null;
    }

    protected DfsTraversalNode getUnvisitedNextNode(DfsTraversalNode src, ExecutorService execSvc) throws ViolatingCycleFoundException, Exception {
        while (src.getEventIterator().hasNext()) {
            final BEvent nextEvent = src.getEventIterator().next();
            DfsTraversalNode possibleNextNode = src.getNextNode(nextEvent, execSvc);
            visitedEdgeCount++;
            if (visited.isVisited(possibleNextNode.getSystemState()) ) {
                // Found a possible cycle                
                BProgramSyncSnapshot pns = possibleNextNode.getSystemState();
                
                for ( int idx=0; idx<currentPath.size(); idx++) {
                    DfsTraversalNode nd = currentPath.get(idx);
                    if ( pns.equals(nd.getSystemState()) ) {
                        // found an actual cycle
                        trace.cycleTo(nextEvent, idx);
                        Set<Violation> res = inspections.stream().map(i->i.inspectTrace(trace))
                            .filter(o->o.isPresent()).map(Optional::get).collect(toSet());

                        for ( Violation v : res ) {
                            if ( ! listener.violationFound(v, this)) {
                                throw new ViolatingCycleFoundException(v);
                            }
                        }
                    }
                }                    
            } else {
                // advance to this newly discovered node
                return possibleNextNode;
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

    public void setVisitedNodeStore(VisitedStateStore aVisitedNodeStore) {
        visited = aVisitedNodeStore;
    }

    public VisitedStateStore getVisitedNodeStore() {
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
        if ( trace.getStateCount() == 0 ) {
            trace.push( n.getSystemState() );
        } else {
            trace.advance(n.getLastEvent(), n.getSystemState());
        }
    }

    private void pop() {
        currentPath.remove(currentPath.size() - 1);
        trace.pop();
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
    
    public void addInspection( ExecutionTraceInspection ins ) {
        inspections.add(ins);
    }
    
    public Set<ExecutionTraceInspection> getInspections() {
        return inspections;
    }
    
    public boolean removeInspection( ExecutionTraceInspection ins ) {
        return inspections.remove(ins);
    }

}
