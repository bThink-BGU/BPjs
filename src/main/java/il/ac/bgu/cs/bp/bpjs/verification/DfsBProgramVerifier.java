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
import il.ac.bgu.cs.bp.bpjs.search.Node;
import il.ac.bgu.cs.bp.bpjs.search.StateHashVisitedNodeStore;
import il.ac.bgu.cs.bp.bpjs.search.VisitedNodeStorage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    
    private final VisitedNodeStorage visited = new StateHashVisitedNodeStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;

    public VerificationResult verify( BProgram aBp ) throws Exception {
        visitedStatesCount=1;
        long start = System.currentTimeMillis();
        List<Node> counterEx = dfsUsingStack(Node.getInitialNode(aBp));
        long end = System.currentTimeMillis();
        return new VerificationResult(counterEx, end-start, visitedStatesCount);
    }
    
    protected List<Node> dfsUsingStack(Node node) throws Exception {
		ArrayDeque<Node> pathNodes = new ArrayDeque<>(); // Current program stack.

		visited.store(node);
		pathNodes.add(node);
        
        long iterationCount = 0;
		while (!pathNodes.isEmpty()) {
            iterationCount++;
			node = pathNodes.peek();

			boolean canGoForward = false;

			while (node.getEventIterator().hasNext()) {

				BEvent e = node.getEventIterator().next();

				Node nextNode = node.getNextNode(e);

				if (!visited.isVisited(nextNode) ) {
					visitedStatesCount++;
					canGoForward = true;
                    
					visited.store(nextNode);
					pathNodes.add(nextNode);

					if (!nextNode.check()) {
						// Found a problematic path :-)
						return new ArrayList<>(Arrays.asList(pathNodes.toArray(new Node[0])));
					}

					break;
				}
			}

            if ( iterationCount%1000==0 ) {
                // TODO - switch to listener architecture.
                System.out.printf("~ %,d states scanned (iteration %,d)\n", visitedStatesCount, iterationCount);
            }
			if (!canGoForward || pathNodes.size() >= maxTraceLength) {
				pathNodes.pop();
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

}
