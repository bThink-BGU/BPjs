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

//	@Test
	public void test2() throws InterruptedException, IOException, ClassNotFoundException {

		String SRC = "" + //
				"bp.registerBThread('', function() {\n" + //
                "  bp.log.info('Inside the bt');\n" + //
                "  var f=function(){bp.log.info('hello from f');}; " + //
                "  var es = bp.EventSet('none',function(e){return true;});\n" + //
				"  bsync({ waitFor : es });\n" + //
                "  f(); \n " + //
                "  bp.log.info('post-first-bSync');\n" + //
//                "  bp.log.info('e=' + e);\n" + //
				"  var e=bsync({ waitFor : bp.Event('A') });\n" + //
                "  bp.log.info('after 1st A');\n" + //
//                "  bp.log.info('e=' + e);\n" + //
				"  bsync({ waitFor : bp.Event('A') });\n" + //
//                "  e=e.name;" + 
                "  bp.log.info('after 2nd A');\n" + 
				"  bsync({ waitFor : bp.Event('A') });\n" + //
                "  bp.log.info('after 2nd A');\n" +
				"});\n"
                + "bp.log.info('setup done.');";

		// Create a program
		BProgram bprog = new StringBProgram(SRC);

		// Get the initial state
        BProgramSyncSnapshot seed = bprog.setup().start();

		// three event orders we're about to explore
		List<List<String>> eventOrderings = Arrays.asList( //
				Arrays.asList("A", "A", "A"), //
				Arrays.asList("A", "A", "A", "A"), //
				Arrays.asList("A", "A", "A", "D")//
		);

		// explore each event ordering
		for (List<String> events : eventOrderings) {
			System.out.println("Running event set: " + events);

			BProgramSyncSnapshot cur = seed;

			for (String s : events) {
                System.out.println("Event " + s);
				BProgramSyncSnapshot dup = BProgramSyncSnapshotCloner.clone(cur);
                cur = dup.triggerEvent(new BEvent(s));
			}
			System.out.println("..Doneֿ\n\n\n");
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

	}

	// Iterative DFS using stack
	public static void dfsUsingStack(Node node) throws Exception {
		Stack<Node> path_nodes = new Stack<>();
		Set<Node> visited_nodes = new HashSet<>();

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
