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

import java.util.*;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * Takes a {@link BProgram}, and verifies that it does not run into false 
 * assertions, given all possible event selections.
 * Take care to use the appropriate {@link VisitedStateStore} for the 
 * {@link BProgram} being verified.
 *
 * States are scanned using a DFS.
 *
 * @author michael
 */
public class DfsBProgramVerifier {

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    public final static long DEFAULT_MAX_TRACE = 100;

    /**
     * Default number of iterations between invocation of {@link ProgressListener#iterationCount(long, long, il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier)
     * }.
     */
    public final static long DEFAULT_ITERATION_COUNT_GAP = 1000;

    /**
     * A listener to the progress of the DFS state scanning.
     */
    public interface ProgressListener {

        void started(DfsBProgramVerifier v);

        void iterationCount(long count, long statesHit, DfsBProgramVerifier v);

        void maxTraceLengthHit(List<Node> trace, DfsBProgramVerifier v);

        void done(DfsBProgramVerifier v);
    }

    private long visitedStatesCount;
    private VisitedStateStore visited = new BProgramStateVisitedStateStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;
    private final ArrayList<Node> currentPath = new ArrayList<>();
    private Optional<ProgressListener> listenerOpt = Optional.empty();
    private long iterationCountGap = DEFAULT_ITERATION_COUNT_GAP;
    private BProgram currentBProgram;
    private boolean debugMode = false;
    private boolean detectDeadlocks = true;

    private final ArrayList<BEvent> invalidEvents = new ArrayList<>();


    public VerificationResult verify(BProgram aBp) throws Exception {
        currentBProgram = aBp;
        visitedStatesCount = 1;
        currentPath.clear();
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("DfsBProgramRunner-" + INSTANCE_COUNTER.incrementAndGet());
        long start = System.currentTimeMillis();
        listenerOpt.ifPresent(l -> l.started(this));
        VerificationResult vr = dfsUsingStack(Node.getInitialNode(aBp, execSvc), execSvc);
        long end = System.currentTimeMillis();
        listenerOpt.ifPresent(l -> l.done(this));
        execSvc.shutdown();
        return new VerificationResult(vr.getViolationType(), vr.getFailedAssertion(),vr.getInvalidEvent(), vr.getCounterExampleTrace(), end - start, visitedStatesCount);
    }

    protected VerificationResult dfsUsingStack(Node aStartNode, ExecutorService execSvc) throws Exception {
        long iterationCount = 0;
        visitedStatesCount = 0;

        visited.store(aStartNode);
        push(aStartNode);

        while (!isPathEmpty()) {
            if (debugMode) {
                printStatus(iterationCount, Collections.unmodifiableList(currentPath));
            }

            Node curNode = peek();
            if (curNode != null) {
                if (isDetectDeadlocks() &&
                        hasRequestedEvents(curNode.getSystemState()) &&
                        curNode.getSelectableEvents().isEmpty()
                        ) {
                    // detected deadlock
                    return new VerificationResult(VerificationResult.ViolationType.Deadlock, currentPath);
                }
                if ((!this.invalidEvents.isEmpty()) &&
                        hasInvalidEvent(curNode.getSystemState())
                        ) {
                    //Detected possible bad state
                    return new VerificationResult(VerificationResult.ViolationType.BadState,
                            getInvalidEvent(curNode.getSystemState()),
                            currentPath);
                }
                if (!curNode.getSystemState().isStateValid()) {
                    // detected assertion failure.
                    return new VerificationResult(VerificationResult.ViolationType.FailedAssertion,
                            curNode.getSystemState().getFailedAssertion(),
                            currentPath);
                }

            }
            iterationCount++;

            if (pathLength() == maxTraceLength) {
                listenerOpt.ifPresent(progressListener -> progressListener.maxTraceLengthHit(currentPath, this));
                // fold stack;
                pop();

            } else {
                Node nextNode = getUnvisitedNextNode(curNode, execSvc);
                if (nextNode == null) {
                    // fold stack, retry next iteration;
                    pop();
                    if (isDebugMode()) {
                        System.out.println("-pop!-");
                    }
                } else {
                    // go deeper 
                    visited.store(nextNode);
                    if (isDebugMode()) {
                        System.out.println("-visiting: " + nextNode);
                    }
                    push(nextNode);
                    visitedStatesCount++;
                }
            }

            if ((iterationCount % iterationCountGap == 0) && (listenerOpt.isPresent())) {
                listenerOpt.get().iterationCount(iterationCount, visitedStatesCount, this);
            }
        }

        return new VerificationResult(VerificationResult.ViolationType.None, null);
    }

    protected Node getUnvisitedNextNode(Node src, ExecutorService execSvc) throws Exception {
        while (src.getEventIterator().hasNext()) {
            final BEvent nextEvent = src.getEventIterator().next();
            Node possibleNextNode = src.getNextNode(nextEvent, execSvc);
            if (!visited.isVisited(possibleNextNode)) {
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

    void printStatus(long iteration, List<Node> path) {
        System.out.println("Iteration " + iteration);
        System.out.println("  visited: " + visitedStatesCount);
        path.forEach(n -> System.out.println("  " + n.getLastEvent()));
    }

    private void push(Node n) {
        currentPath.add(n);
    }

    private int pathLength() {
        return currentPath.size();
    }

    private boolean isPathEmpty() {
        return pathLength() == 0;
    }

    private Node peek() {
        return isPathEmpty() ? null : currentPath.get(currentPath.size() - 1);
    }

    private Node pop() {
        return currentPath.remove(currentPath.size() - 1);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDetectDeadlocks() {
        return detectDeadlocks;
    }

    public void setDetectDeadlocks(boolean detectDeadlocks) {
        this.detectDeadlocks = detectDeadlocks;
    }

    public void addInvalidEvent(BEvent desiredEvent) {
        invalidEvents.add(desiredEvent);
    }

    public ArrayList<BEvent> getInvalidEvents() {
        return invalidEvents;
    }

    private boolean hasRequestedEvents(BProgramSyncSnapshot bpss) {
        return bpss.getBThreadSnapshots().stream().anyMatch(btss -> (!btss.getBSyncStatement().getRequest().isEmpty()));
    }

    private boolean hasInvalidEvent(BProgramSyncSnapshot bpss) {
        Set<String> names = invalidEvents.stream().map(x -> x.name).collect(Collectors.toSet());
        Set<? extends BEvent> possibleEvents = bpss.getBThreadSnapshots().stream().map(btss -> btss.getBSyncStatement().getRequest()).flatMap(Collection::stream).collect(Collectors.toSet());

        return possibleEvents.stream().anyMatch(x -> names.contains(x.name));
    }

    /**
     * Returns the invalid event, must be called only after hasInvalidEvent returns true.
     * @param bpss
     * @return
     */
    private BEvent getInvalidEvent(BProgramSyncSnapshot bpss) {
        Set<String> names = invalidEvents.stream().map(x -> x.name).collect(Collectors.toSet());
        Set<? extends BEvent> possibleEvents = bpss.getBThreadSnapshots().stream().map(btss -> btss.getBSyncStatement().getRequest()).flatMap(Collection::stream).collect(Collectors.toSet());

        return possibleEvents.stream().filter(x -> names.contains(x.name)).collect(Collectors.toList()).get(0);
    }
}
