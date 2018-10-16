package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import javax.swing.JButton;
import javax.swing.JFrame;


import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;

/**
 * Runs the TicTacToe game in interactive mode, with GUI.
 * 
 * @author reututy
 */
class TicTacToeGameMain extends JFrame {

	// GUI for interactively playing the game
	public static TTTDisplayGame TTTdisplayGame;

	public static void main(String[] args) throws InterruptedException {

		// Create a program
		BProgram bprog = new SingleResourceBProgram("BPJSTicTacToe.js");

		bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
		bprog.setWaitForExternalEvents(true);
		JFrame f = new TicTacToeGameMain();
		BProgramRunner rnr = new BProgramRunner(bprog);
		rnr.addListener(new PrintBProgramRunnerListener());
		TTTdisplayGame = new TTTDisplayGame(bprog, rnr);
		rnr.run();
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
