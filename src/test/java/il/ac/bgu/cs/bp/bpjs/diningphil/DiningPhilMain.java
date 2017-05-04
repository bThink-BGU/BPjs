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
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.BProgramSyncSnapshotCloner;

public class DiningPhilMain {

	@Test
	public void test2() throws InterruptedException, IOException, ClassNotFoundException {

		// 1. Setup the program
		BProgram simpleProg = new SingleResourceBProgram("BPJSDiningPhil.js");
		BProgramSyncSnapshot seed = simpleProg.setup().start(); // seed is after
																// BThreads are
																// registered
																// and before
																// they run.

		BProgramSyncSnapshotCloner cloner = new BProgramSyncSnapshotCloner();

		// three event orders we're about to explore
		List<List<String>> eventOrderings = Arrays.asList(
				Arrays.asList("A", "B", "C"),
				Arrays.asList("P1R", "P1L", "R1L", "R1R"));

		// explore each event ordering
		for (List<String> events : eventOrderings) {
			System.out.println("Running event set: " + events);
			BProgramSyncSnapshot cur = cloner.clone(seed); // get a fresh copy
			for (String s : events) {
                System.out.println("Event " + s);
				cur = cloner.clone(cur);
                System.out.println("  cloned");
                cur.triggerEvent(new BEvent(s));
                System.out.println("  done");
//				cur = cur.triggerEvent(new BEvent(s));
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

				System.out.println("Element=" + element);
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
