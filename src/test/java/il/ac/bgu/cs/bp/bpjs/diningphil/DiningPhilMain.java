package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.BProgramSyncSnapshotCloner;

public class DiningPhilMain {

	@Test
	public void test2() throws InterruptedException, IOException, ClassNotFoundException {

		String SRC = "" + //

				"bp.registerBThread('', function() {" + //
				"  bp.log.info('1');" + //
				"  var e1 = bsync({ waitFor : bp.Event('A') });" + //
				"  bp.log.info('2');" + //
				"  bsync({ waitFor : bp.Event('A') });" + //
				"  bsync({ waitFor : bp.Event('A') });" + //
				"});";

		// Create a program
		BProgram bprog = new StringBProgram(SRC);

		// Get the initial state
		BProgramSyncSnapshot seed = bprog.setup();
		seed.start();

		// three event orders we're about to explore
		List<List<String>> eventOrderings = Arrays.asList( //
				// Arrays.asList(), //
				// Arrays.asList("A", "A", "D"), //
				// Arrays.asList("A", "A", "A", "D"), //
				// Arrays.asList("A", "D"), //
				Arrays.asList("A", "A", "A", "A", "A", "D")//
		);

		// explore each event ordering
		for (List<String> events : eventOrderings) {
			System.out.println("Running event set: " + events);

			BProgramSyncSnapshot cur = BProgramSyncSnapshotCloner.clone(seed.start());

			for (String s : events) {
				cur = BProgramSyncSnapshotCloner.clone(cur).triggerEvent(new BEvent(s));
			}
			System.out.println("..Done");
		}
	}

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");

		// DFS
		try {
			dfsUsingStack(Node.getInitialNode(bprog));

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("..Done");

	}

	// Iterative DFS using stack
	public static void dfsUsingStack(Node node) throws Exception {
		Node initial = node;

		Stack<Node> path_nodes = new Stack<Node>();
		Set<Node> visited_nodes = new HashSet<Node>();

		visited_nodes.add(node);
		path_nodes.add(node);

		while (!path_nodes.isEmpty()) {

			node = path_nodes.peek();

			// This flag remains false if node doesn't have an unvisited
			// follower
			boolean flag = false;

			// loop: for (BEvent e : node.getPossibleEvents()) {
			loop: while (node.getEventIterator().hasNext()) {

				BEvent e = node.getEventIterator().next();
				if (node == initial) {
					System.out.println("\te=" + e);
				}

				Node nextNode = node.getNextNode(e);
				if (!visited_nodes.contains(nextNode)) {
					flag = true;

					visited_nodes.add(nextNode);
					path_nodes.add(nextNode);

					// System.out.println("Node="+node);

					if (!nextNode.check()) {
						// Found a problematic path :-)
						throw new BadTraceException(path_nodes);
					}

					break loop;
				}

			}

			if (!flag) {
				path_nodes.pop();
			}
		}
	}

}
