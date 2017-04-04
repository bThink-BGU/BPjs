package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;

public class DiningPhilMain {

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		//String SRC = "//home//reututy//workspace//gitBPResearch//BPjs//src//est//resources//BPJSDiningPhil.js";
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");
		//BProgram bprog = new StringBProgram(SRC);
        //BProgram bprog = new StringBProgram("BPJSDiningPhil.js");
        
		//bprog.addListener(new StreamLoggerListener());
        // Run the top-level code (b-threads are registered but not yet run)
        BProgramSyncSnapshot cur = bprog.setup();
        
        // Run to first bsync
        cur = cur.start();
        
//        // Get a snapshot
//        final BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
//        System.out.println(snapshot);
//        
//        // Serialize snapshot
//        byte[] serializedContinuationAndScope = null;
//        Object bp = null;
        
		// DFS
		// dfsUsingStack(Node.getInitialNode(bprog));
		try {
			dfsUsingStack(new NodeStub2("B,B"));
		} catch (BadTraceException e) {
			e.printStackTrace();
		}

	}

	// Iterative DFS using stack
	public static void dfsUsingStack(Node node) throws BadTraceException {
		Stack<Node> path_nodes = new Stack<Node>(); 
		Set<Node> visited_nodes = new HashSet<Node>();

		visited_nodes.add(node);
		path_nodes.add(node);

		while (!path_nodes.isEmpty()) {

			Node element = path_nodes.peek();
			System.out.println("Element="+element);
			boolean flag = false;

			loop: for (BEvent e : element.getPossibleEvents()) {
				Node n = element.getNextNode(e);
					if (!visited_nodes.contains(n)) {
						flag = true;
						
						visited_nodes.add(n);
						path_nodes.add(n);
						
						System.out.println("Pushing " + n);

						if (!n.check()) {
							path_nodes.add(n); //I added
							throw new BadTraceException(path_nodes); //the problematic path
						}
						
						break loop;
					}
				
			}

			if (!flag) {
				System.out.println("poping " + element);
				path_nodes.pop();
			}
		}
	}

}
