package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;

public class DiningPhilMain {

	public final static long MAX_PATH = 100;

	private static long count = 1;

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");

		long start = System.currentTimeMillis();

		try {
			dfsUsingStack(Node.getInitialNode(bprog));

			System.out.println("No error :-)");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		System.out.println("Scanned " + count + " states");
		System.out.println("Time:" + (System.currentTimeMillis() - start) / 1000 + " seconds");

	}

	// Iterative DFS using stack
	public static void dfsUsingStack(Node node) throws Exception {
		Stack<Node> path_nodes = new Stack<>(); // The bad trace
		Set<Node> visited_nodes = new HashSet<>(); // All the visited nodes' id

		visited_nodes.add(node);
		path_nodes.add(node);

		while (!path_nodes.isEmpty()) {

			node = path_nodes.peek();

			// This flag remains false if node doesn't have an unvisited
			// follower
			boolean flag = false;

			loop: while (node.getEventIterator().hasNext()) {

				BEvent e = node.getEventIterator().next();

				Node nextNode = node.getNextNode(e);

				if (!visited_nodes.contains(nextNode)) {
					count++;
					flag = true;
                    
					visited_nodes.add(nextNode);
					path_nodes.add(nextNode);

					if (!nextNode.check()) {
						// Found a problematic path :-)
						throw new BadTraceException(path_nodes);
					}

					break loop;
				}

			}

			if (!flag || path_nodes.size() >= MAX_PATH) {
				path_nodes.pop();
			}
		}

	}

}
