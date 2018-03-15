package il.ac.bgu.cs.bp.bpjs.TicTacToe;


import javax.swing.JButton;
import javax.swing.JFrame;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.mozilla.javascript.Scriptable;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;

/**
 * For Gaming mode change UseSimulatedPlayer to false. For Model Checking mode
 * change UseSimulatedPlayer to true.
 * 
 * @author reututy
 */
class TicTacToeGameMain extends JFrame {

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
			rnr.start();
		} else {
			System.out.println("Creating SimulatedPlayer");

			String SimulatedPlayer = "	" +
//										+ "bp.registerBThread('STAM', function() {\n" +
//										"while (true) {\n" +
//											"bsync({ request:[ bp.Event('STAM') ]\n" +
//											//" , interrupt:[ StaticEvents.XWin]\n" +
//												"});\n" +
//											"}\n" +
//										"});\n" +			
										"bp.registerBThread('XMoves', function() {\n" +
										"while (true) {\n" +
											"bsync({ request:[ X(0, 0), X(0, 1), X(0, 2), X(1, 0), \n" +
											"X(1, 1), X(1, 2), X(2, 0), X(2, 1), X(2, 2) ] }, 10); \n" +
											"}\n" +
										"});\n";

	        bprog.appendSource(SimulatedPlayer);

			try {
				DfsBProgramVerifier vfr = new DfsBProgramVerifier();
				
				vfr.setMaxTraceLength(50);
				vfr.setDebugMode(true);
				vfr.setDetectDeadlocks(false);
				vfr.addInvalidEvent(BEvent.named("Draw"));
				vfr.setDetectInvalidStates(true);
				vfr.getInvalidEvents().forEach(System.out::println);

				final VerificationResult res = vfr.verify(bprog);
				if (res.isCounterExampleFound()) {
					System.out.println("Found a counterexample");
					System.out.println("Counter example type is: " + res.getViolationType().name());
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

@SuppressWarnings("serial")
class TTTButton extends JButton {
	int row;
	int col;

	/**
	 * Constructor.
	 *
	 * @param row
	 *            The row of the button.
	 * @param col
	 *            The column of the button.
	 */
	public TTTButton(int row, int col) {
		super();
		this.row = row;
		this.col = col;
	}
}
