package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;

public class DiningPhilMain {

	public final static long MAX_PATH = 100;

	private static long visitedStatesCount = 1;

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");

		long start = System.currentTimeMillis();
        
        bprog.putInGlobalScope("PHILOSOPHER_COUNT", 10);
        long end=0;
        
		try {
			dfsUsingStack(Node.getInitialNode(bprog));
            end = System.currentTimeMillis();
			System.out.println("No violating trace found.");
            
		} catch (BadTraceException bte) {
            end = System.currentTimeMillis();
            System.out.println("Found a violating trace:");
			bte.getBadTrace().forEach( nd -> System.out.println(" " + nd.getLastEvent()));
            
		} catch (Exception ex) {
            ex.printStackTrace(System.out);
        }

		System.out.printf("Scanned %,d states\n", visitedStatesCount );
		System.out.printf("Time: %,d milliseconds\n", end - start);

	}

	// Iterative DFS using stack
	public static void dfsUsingStack(Node node) throws BadTraceException, Exception {
		Stack<Node> pathNodes = new Stack<>(); // The bad trace
		Set<Node> visitesNodes = new HashSet<>(); // All the visited nodes' id

		visitesNodes.add(node);
		pathNodes.add(node);

		while (!pathNodes.isEmpty()) {

			node = pathNodes.peek();

			// This flag remains false if node doesn't have an unvisited
			// follower
			boolean flag = false;

			while (node.getEventIterator().hasNext()) {

				BEvent e = node.getEventIterator().next();

				Node nextNode = node.getNextNode(e);

				if (!visitesNodes.contains(nextNode)) {
					visitedStatesCount++;
					flag = true;
                    
					visitesNodes.add(nextNode);
					pathNodes.add(nextNode);

					if (!nextNode.check()) {
						// Found a problematic path :-)
						throw new BadTraceException(pathNodes);
					}

					break;
				}
                if ( visitedStatesCount%10000==0 ) {
                    System.out.printf("~ %,d states scanned\n", visitedStatesCount);
                }

			}

			if (!flag || pathNodes.size() >= MAX_PATH) {
				pathNodes.pop();
			}
		}

	}

}
