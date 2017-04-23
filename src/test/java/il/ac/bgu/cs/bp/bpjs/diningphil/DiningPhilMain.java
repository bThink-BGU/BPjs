package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;

public class DiningPhilMain {

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");

		// DFS
		try {

			dfsUsingStack(Node.getInitialNode(bprog));

			// dfsUsingStack(new NodeStub2("B,B"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Iterative DFS using stack
	public static void dfsUsingStack(Node node) throws Exception {
		Stack<Node> path_nodes = new Stack<Node>();
		Set<Node> visited_nodes = new HashSet<Node>();

		visited_nodes.add(node);
		path_nodes.add(node);

		while (!path_nodes.isEmpty()) {

			Node element = path_nodes.peek();
			// System.out.println("Element=" + element);
			boolean flag = false;

			loop: for (BEvent e : element.getPossibleEvents()) {

				System.out.println("element=" + element);
				System.out.println("e=" + e);
				Node n = element.getNextNode(e);
				if (!visited_nodes.contains(n)) {
					flag = true;

					visited_nodes.add(n);
					path_nodes.add(n);

					if (!n.check()) {
						// Found a problematic path :-)
						throw new BadTraceException(path_nodes);
					}

					break loop;
				}

			}

			if (!flag) {
				System.out.println("popping " + element);
				path_nodes.pop();
			}
		}
	}

}
