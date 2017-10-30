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

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.HashVisitedNodeStore;
import il.ac.bgu.cs.bp.bpjs.search.Node;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import il.ac.bgu.cs.bp.bpjs.search.VisitedNodeStore;
import java.util.Deque;

/**
 * 
 * Takes a BProgram, and verifies it does not get into a deadlock (i.e there
 * is always a selectable state). If the verification fails, returns a trace
 * that serves as a counter example.
 * 
 * States are scanned using a DFS.
 * 
 * @author michael
 */
public class DfsBProgramVerifier {

    public final static long DEFAULT_MAX_TRACE = 100;

	private long visitedStatesCount;
    
    private VisitedNodeStore visited = new HashVisitedNodeStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;

    public VerificationResult verify( BProgram aBp ) throws Exception {
        visitedStatesCount=1;
        long start = System.currentTimeMillis();
        List<Node> counterEx = dfsUsingStack(Node.getInitialNode(aBp));
        long end = System.currentTimeMillis();
        return new VerificationResult(counterEx, end-start, visitedStatesCount);
    }
    
    protected List<Node> dfsUsingStack(Node aStartNode) throws Exception {
		ArrayDeque<Node> pathNodes = new ArrayDeque<>(); // Current execution stack.

        long iterationCount = 0;
		visited.store(aStartNode);
		pathNodes.push(aStartNode);
        
		while ( !pathNodes.isEmpty() ) {
            printStatus(iterationCount, pathNodes);
            iterationCount++;
			Node curNode = pathNodes.peek();
            
            if ( ! curNode.check() ) {
                // Found a problematic path :-)
                final ArrayList<Node> counterExampleTrace = new ArrayList<>(Arrays.asList(pathNodes.toArray(new Node[0])));
                Collections.reverse(counterExampleTrace);
                return counterExampleTrace;
            }            
            
            if ( pathNodes.size() == maxTraceLength ) {
                // fold stack;
                pathNodes.pop();
                
            } else {
                Node nextNode = getUnvisitedNextNode(curNode);
                if ( nextNode == null ) {
                    // fold stack, retry next iteration;
                    pathNodes.pop();
                    
                } else {
                    // go deeper 
                    visited.store(nextNode);
                    pathNodes.push(nextNode);
                    visitedStatesCount++;
                }
            }
            
            if ( iterationCount%1000==0 ) {
                // TODO - switch to listener architecture.
                System.out.printf("~ %,d states scanned (iteration %,d)\n", visitedStatesCount, iterationCount);
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
           
    void printStatus( long iteration, Deque<Node> path) {
        System.out.println("Iteration " + iteration );
        System.out.println("  visited: " + visitedStatesCount );
        path.forEach( n -> System.out.println("  " + n.getLastEvent()));
    }
    
}
