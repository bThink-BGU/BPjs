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
import java.util.Collections;
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
     * Default number of iterations between invocation of
     * {@link ProgressListener#iterationCount(long, long, il.ac.bgu.cs.bp.bpjs.verification.DfsBProgramVerifier)}.
     */
    public final static long DEFAULT_ITERATION_COUNT_GAP = 1000;

    /**
     * A listener to the progress of the DFS state scanning.
     */
    public static interface ProgressListener {

        void started(DfsBProgramVerifier v);

        void iterationCount(long count, long statesHit, DfsBProgramVerifier v);

        void maxTraceLengthHit(List<DfsTraversalNode> trace, DfsBProgramVerifier v);

        void done(DfsBProgramVerifier v);
    }
    
    private static class ViolatingCycleFoundException extends Exception{
        final Violation v;

        public ViolatingCycleFoundException(Violation v) {
            this.v = v;
        }
    }
    
    private long visitedEdgeCount;
    private VisitedStateStore visited = new BThreadSnapshotVisitedStateStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;
    private final List<DfsTraversalNode> currentPath = new ArrayList<>();
    private Optional<ProgressListener> listenerOpt = Optional.empty();
    private long iterationCountGap = DEFAULT_ITERATION_COUNT_GAP;
    private BProgram currentBProgram;
    private boolean debugMode = false;
    private final Set<DfsTraceInspection> traceInspectors = new HashSet<>();
    private final Set<DfsCycleInspection> cycleInspectors = new HashSet<>();

    public VerificationResult verify(BProgram aBp) throws Exception {
        currentBProgram = aBp;
        visitedEdgeCount = 0;
        currentPath.clear();
        visited.clear();
        
        // in case no verifications were specified, use the defauls set.
        if ( traceInspectors.isEmpty() && cycleInspectors.isEmpty() ) { 
            traceInspectors.addAll( DfsInspections.ALL_TRACE );
            cycleInspectors.add(DfsInspections.HotBThreadCycles );
        }
        
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("DfsBProgramRunner-" + INSTANCE_COUNTER.incrementAndGet());
        long start = System.currentTimeMillis();
        listenerOpt.ifPresent(l -> l.started(this));
        Violation vio = dfsUsingStack(DfsTraversalNode.getInitialNode(aBp, execSvc), execSvc);
        long end = System.currentTimeMillis();
        listenerOpt.ifPresent(l -> l.done(this));
        execSvc.shutdown();
        return new VerificationResult(vio, end - start, visited.getVisitedStateCount(), visitedEdgeCount);
    }

    protected Violation dfsUsingStack(DfsTraversalNode aStartNode, ExecutorService execSvc) throws Exception {
        long iterationCount = 0;

        push(aStartNode);

        while (!isPathEmpty()) {
            if (debugMode) {
                printStatus(iterationCount, Collections.unmodifiableList(currentPath));
            }

            DfsTraversalNode curNode = peek();
            if (curNode != null) {
                Optional<Violation> res = traceInspectors.stream()
                        .map(v->v.inspectTrace(currentPath))
                        .filter(o->o.isPresent()).map(Optional::get)
                        .findAny();
                if ( res.isPresent() ) {
                    return res.get();
                }
            }
            iterationCount++;

            if (pathLength() == maxTraceLength) {
                if (listenerOpt.isPresent()) {
                    listenerOpt.get().maxTraceLengthHit(currentPath, this);
                }
                // fold stack;
                pop();

            } else {
                try {
                    DfsTraversalNode nextNode = getUnvisitedNextNode(curNode, execSvc);
                    if (nextNode == null) {
                        // fold stack, retry next iteration;
                        pop();
                        if (isDebugMode()) {
                            System.out.println("-pop!-");
                        }
                    } else {
                        // go deeper 
                        if (isDebugMode()) {
                            System.out.println("-visiting: " + nextNode);
                        }
                        push(nextNode);
                    }
                } catch (ViolatingCycleFoundException vcfe ) {
                    return vcfe.v;
                }
            }

            if (iterationCount % iterationCountGap == 0 && listenerOpt.isPresent()) {
                listenerOpt.get().iterationCount(iterationCount, visited.getVisitedStateCount(), this);
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
                if ( !cycleInspectors.isEmpty() ) {
                    BProgramSyncSnapshot pns = possibleNextNode.getSystemState();
                    final AtomicInteger idx = new AtomicInteger();
                    for ( DfsTraversalNode nd : currentPath ){
                        if ( pns.equals(nd.getSystemState()) ) {
                            // found an actual cycle
                            Optional<Violation> res = cycleInspectors.stream().map(i->i.inspectCycle(currentPath, idx.intValue(), nextEvent))
                                .filter(o->o.isPresent()).map(Optional::get)
                                .findAny();
                            if ( res.isPresent() ) {
                                throw new ViolatingCycleFoundException(res.get());
                            }
                        }
                        idx.incrementAndGet();
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
        listenerOpt = Optional.of(pl);
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

    private void push(DfsTraversalNode n) {
        visited.store(n.getSystemState());
        currentPath.add(n);
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

    private DfsTraversalNode pop() {
        return currentPath.remove(currentPath.size() - 1);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public void addInspector( DfsTraceInspection ins ) {
        traceInspectors.add(ins);
    }
    
    public Set<DfsTraceInspection> getTraceInspectors() {
        return traceInspectors;
    }
    
    public boolean removeInspector( DfsTraceInspection ins ) {
        return traceInspectors.remove(ins);
    }

    public void addInspector( DfsCycleInspection ins ) {
        cycleInspectors.add(ins);
    }
    
    public Set<DfsCycleInspection> getCycleInspectors() {
        return cycleInspectors;
    }
    
    public boolean removeInspector( DfsCycleInspection ins ) {
        return cycleInspectors.remove(ins);
    }
}
