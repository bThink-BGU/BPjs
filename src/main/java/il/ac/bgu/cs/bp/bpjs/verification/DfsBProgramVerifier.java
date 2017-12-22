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
package il.ac.bgu.cs.bp.bpjs.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.FullVisitedNodeStore;
import il.ac.bgu.cs.bp.bpjs.search.Node;
import il.ac.bgu.cs.bp.bpjs.search.VisitedNodeStore;
import il.ac.bgu.cs.bp.bpjs.verification.requirements.PathRequirement;
import il.ac.bgu.cs.bp.bpjs.verification.requirements.PathRequirements;
import java.util.Optional;

/**
 * 
 * Takes a {@link BProgram}, and verifies that it conforms to a given {@link PathRequirement}. Take care to use the
 * appropriate {@link VisitedNodeStore} for the {@link BProgram} being verified.
 * 
 * States are scanned using a DFS.
 * 
 * @author michael
 */
public class DfsBProgramVerifier {

    public final static long DEFAULT_MAX_TRACE = 100;
    
    /** 
     * Default number of iterations between invocation of {@link ProgressListener#iterationCount(long, long, il.ac.bgu.cs.bp.bpjs.verification.DfsBProgramVerifier) }.
     */
    public final static long DEFAULT_ITERATION_COUNT_GAP = 1000;
    
    /**
     * A listener to the progress of the DFS state scanning.
     */
    public static interface ProgressListener {
        void started( DfsBProgramVerifier v );
        void iterationCount( long count, long statesHit, DfsBProgramVerifier v );
        void maxTraceLengthHit( List<Node> trace, DfsBProgramVerifier v );
        void done( DfsBProgramVerifier v );
    }
    
	private long visitedStatesCount;
    private VisitedNodeStore visited = new FullVisitedNodeStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;
    private final ArrayList<Node> currentPath = new ArrayList<>();
    private Optional<ProgressListener> listenerOpt = Optional.empty();
    private long iterationCountGap = DEFAULT_ITERATION_COUNT_GAP;
    private BProgram currentBProgram;
    private boolean debugMode = false;
    
    /**
     * The requirement {@code this} verifier will test a {@link BProgram} for.
     */
    private PathRequirement requirement = PathRequirements.NO_DEADLOCK;
    
    public VerificationResult verify( BProgram aBp ) throws Exception {
        currentBProgram = aBp;
        visitedStatesCount=1;
        currentPath.clear();
        long start = System.currentTimeMillis();
        listenerOpt.ifPresent(l->l.started(this));
        List<Node> counterEx = dfsUsingStack(Node.getInitialNode(aBp));
        long end = System.currentTimeMillis();
        listenerOpt.ifPresent(l->l.done(this));
        return new VerificationResult(counterEx, end-start, visitedStatesCount);
    }
    
    protected List<Node> dfsUsingStack(Node aStartNode) throws Exception {
        long iterationCount = 0;
        visitedStatesCount = 0;
		
        visited.store(aStartNode);
		push(aStartNode);
        
		while ( ! isPathEmpty() ) {
            if ( debugMode ) {
                printStatus(iterationCount,Collections.unmodifiableList(currentPath));
            }
			
            if ( ! requirement.checkConformance(Collections.unmodifiableList(currentPath)) ) {
                // Found a problematic path :-)
                return currentPath;
            }            
            iterationCount++;
			Node curNode = peek();
            
            if ( pathLength()== maxTraceLength ) {
                if ( listenerOpt.isPresent() ){
                    listenerOpt.get().maxTraceLengthHit(currentPath, this);
                }
                // fold stack;
                pop();
                
            } else {
                Node nextNode = getUnvisitedNextNode(curNode);
                if ( nextNode == null ) {
                    // fold stack, retry next iteration;
                    pop();
                    if ( isDebugMode() ){System.out.println("-pop!-");}
                } else {
                    // go deeper 
                    visited.store(nextNode);
                    if ( isDebugMode() ){
                        System.out.println("-visiting: " + nextNode);
                    }
                    push(nextNode);
                    visitedStatesCount++;
                }
            }
            
            if ( iterationCount%iterationCountGap==0 && listenerOpt.isPresent() ) {
                listenerOpt.get().iterationCount(iterationCount, visitedStatesCount, this);
            }
		}
		return null;
	}
    
    protected Node getUnvisitedNextNode(Node src) throws Exception {
        while ( src.getEventIterator().hasNext() ) {
            final BEvent nextEvent = src.getEventIterator().next();
            Node possibleNextNode = src.getNextNode(nextEvent);
            if ( ! visited.isVisited(possibleNextNode) ) {
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
    
    public void setVisitedNodeStore( VisitedNodeStore aVisitedNodeStore ) {
        visited = aVisitedNodeStore;
    }
    
    public VisitedNodeStore getVisitedNodeStore() {
        return visited;
    }

    public void setRequirement(PathRequirement predicate) {
        this.requirement = predicate;
    }

    public PathRequirement getRequirement() {
        return requirement;
    }
    
    public void setProgressListener( ProgressListener pl ) {
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
    
    void printStatus( long iteration, List<Node> path) {
        System.out.println("Iteration " + iteration );
        System.out.println("  visited: " + visitedStatesCount );
        path.forEach( n -> System.out.println("  " + n.getLastEvent()));
    }
    
    private void push( Node n ) {
        currentPath.add(n);
    }
    
    private int pathLength() {
        return currentPath.size();
    }
    
    private boolean isPathEmpty() {
        return pathLength() == 0;
    }
    
    private Node peek() {
        return isPathEmpty() ? null : currentPath.get(currentPath.size()-1);
    }
    
    private Node pop() {
        return currentPath.remove(currentPath.size()-1);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
}
