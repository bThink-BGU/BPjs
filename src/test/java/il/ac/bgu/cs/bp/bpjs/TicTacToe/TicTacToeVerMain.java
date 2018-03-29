package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.mozilla.javascript.Scriptable;

public class TicTacToeVerMain  {

	// GUI for interactively playing the game
	public static TTTDisplayGame TTTdisplayGame;

	public static boolean UseSimulatedPlayer() {
		return true;
	}

	public static void main(String[] args) throws InterruptedException {

		// Create a program
		BProgram bprog = new SingleResourceBProgram("BPJSTicTacToe.js") {
		//BProgram bprog = new SingleResourceBProgram("STAM-TTT.js") {
			@Override
			protected void setupProgramScope(Scriptable scope) {
				putInGlobalScope("UseSimulatedPlayer", UseSimulatedPlayer());
				super.setupProgramScope(scope);
			}
		};

		bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
		bprog.setDaemonMode(true);
		JFrame f = new TicTacToeGameMain();


		if (!UseSimulatedPlayer()) {
			BProgramRunner rnr = new BProgramRunner(bprog);

			rnr.addListener(new PrintBProgramRunnerListener());
			TTTdisplayGame = new TTTDisplayGame(bprog, rnr);
			rnr.run();
		} else {
//			System.out.println("Creating SimulatedPlayer");
//
//			String SimulatedPlayer = "	" +
////										+ "bp.registerBThread('STAM', function() {\n" +
////										"while (true) {\n" +
////											"bsync({ request:[ bp.Event('STAM') ]\n" +
////											//" , interrupt:[ StaticEvents.XWin]\n" +
////												"});\n" +
////											"}\n" +
////										"});\n" +			
//										"bp.registerBThread('XMoves', function() {\n" +
//										"while (true) {\n" +
//											"bsync({ request:[ X(0, 0), X(0, 1), X(0, 2), X(1, 0), \n" +
//											"X(1, 1), X(1, 2), X(2, 0), X(2, 1), X(2, 2) ] }, 10); \n" +
//											"}\n" +
//										"});\n";
//
////	        bprog.appendSource(SimulatedPlayer);
//			bprog.prependSource(SimulatedPlayer);

			try {
				DfsBProgramVerifier vfr = new DfsBProgramVerifier();
				
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
		}

		System.out.println("end of run");
	}

}