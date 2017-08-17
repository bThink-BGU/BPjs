package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.Set;
import java.util.Stack;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.util.TreeSet;

public class DiningPhilMain {

	public final static long MAX_PATH = 100;

	private static long visitedStatesCount = 1;

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");

		long start = System.currentTimeMillis();
        
        int philosopherCount = 9;
        if ( args.length > 1 ) {
            philosopherCount = Integer.parseInt(args[1]);
        }
        bprog.putInGlobalScope("PHILOSOPHER_COUNT", philosopherCount);
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
		Set<Integer> visitesNodes = new TreeSet<>(); // All the visited nodes' id

		visitesNodes.add(node.hashCode());
		pathNodes.add(node);
        
        long iterationCount = 0;
		while (!pathNodes.isEmpty()) {
            iterationCount++;
			node = pathNodes.peek();

			// This flag remains false if node doesn't have an unvisited
			// follower
			boolean flag = false;

			while (node.getEventIterator().hasNext()) {

				BEvent e = node.getEventIterator().next();

				Node nextNode = node.getNextNode(e);

				if (!visitesNodes.contains(nextNode.hashCode())) {
					visitedStatesCount++;
					flag = true;
                    
					visitesNodes.add(nextNode.hashCode());
					pathNodes.add(nextNode);

					if (!nextNode.check()) {
						// Found a problematic path :-)
						throw new BadTraceException(pathNodes);
					}

					break;
				}

			}

            if ( iterationCount%1000==0 ) {
                System.out.printf("~ %,d states scanned (iteration %,d)\n", visitedStatesCount, iterationCount);
            }
			if (!flag || pathNodes.size() >= MAX_PATH) {
				pathNodes.pop();
			}
		}

	}

}
