package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;

import javax.swing.JFrame;

import org.mozilla.javascript.Scriptable;

public class TicTacToeVerMain  {

	// GUI for interactively playing the game
	public static TTTDisplayGame TTTdisplayGame;

	public static void main(String[] args) throws InterruptedException {

		// Create a program
		BProgram bprog = new SingleResourceBProgram("BPJSTicTacToe.js") {
			@Override
			protected void setupProgramScope(Scriptable scope) {
				super.setupProgramScope(scope);
			}
		};

		bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
		bprog.setDaemonMode(true);
		JFrame f = new TicTacToeGameMain();
		
		//It is possible to add this B-Thread too if needed
//		bp.registerBThread("STAM", function() {
//		while (true) {
//			bsync({ request:[ bp.Event("STAM") ]
//			// , interrupt:[ StaticEvents.XWin]
//			});
//		}
//	});
			
		String SimulatedPlayer =   "bp.registerBThread('XMoves', function() {\n" +
									"while (true) {\n" +
										"bsync({ request:[ X(0, 0), X(0, 1), X(0, 2), X(1, 0), \n" +
										"X(1, 1), X(1, 2), X(2, 0), X(2, 1), X(2, 2) ] }, 10); \n" +
										"}\n" +
									"});\n";
		
		String infiBThread =    "bp.registerBThread('STAM', function() {\n" +
								"while (true) {\n" +
									"bsync({ request:[ bp.Event('STAM') ]\n" +
									// , interrupt:[ StaticEvents.XWin]
										"});\n" +
									"}\n" +
								"});\n";
		
		BProgramRunner rnr = new BProgramRunner(bprog);
		rnr.addListener(new PrintBProgramRunnerListener());
		bprog.appendSource(SimulatedPlayer);
		bprog.appendSource(infiBThread);
		rnr.setBProgram(bprog);
//		TTTdisplayGame = new TTTDisplayGame(bprog, rnr);	//For watching the game
		rnr.run();	
			try {
				DfsBProgramVerifier vfr = new DfsBProgramVerifier();
//				vfr.setDetectDeadlocks(false);
				
				vfr.setMaxTraceLength(50);
				vfr.setDebugMode(true);
				
				final VerificationResult res = vfr.verify(bprog);
				if (res.isCounterExampleFound()) {
					System.out.println("Found a counterexample");
					res.getCounterExampleTrace().forEach(nd -> System.out.println(" " + nd.getLastEvent()));

				} else {
					System.out.println("No counterexample found.");
				}
				System.out.printf("Scanned %,d states\n", res.getScannedStatesCount());
				System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies());

			} catch (Exception ex) {
				ex.printStackTrace(System.out);
			}

		System.out.println("end of run");
	}

}