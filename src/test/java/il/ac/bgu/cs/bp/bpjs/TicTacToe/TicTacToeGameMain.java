package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.mozilla.javascript.Scriptable;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import il.ac.bgu.cs.bp.bpjs.eventselection.PrioritizedBSyncEventSelectionStrategy;

/**
 *  For Gaming mode change isModelChecking to false.
 *  For Model Checking mode change isModelChecking to true.
 * @author reututy
 */
class TicTacToeGameMain extends JFrame {

	// GUI for interactively playing the game
	public static TTTDisplayGame TTTdisplayGame;

	public static boolean isModelChecking() {
		return false;
	}

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		BProgram bprog = new SingleResourceBProgram("BPJSTicTacToe.js") {
            @Override
			protected void setupProgramScope(Scriptable scope) {
				putInGlobalScope("isModelChecking", false);
				super.setupProgramScope(scope);
			}
		};
		
		bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
		
		bprog.setDaemonMode(true);
		JFrame f = new TicTacToeGameMain();
		// f.setVisible(true);

		BProgramRunner rnr = new BProgramRunner(bprog);
		rnr.addListener(new StreamLoggerListener());

		TTTdisplayGame = new TTTDisplayGame(bprog, rnr);

		rnr.start();

		System.out.println("end of run");

		// bprog.add(new UpdatePlayingGUI(), 0.1);
		// bprog.add(new UserMove(), 0.5);

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