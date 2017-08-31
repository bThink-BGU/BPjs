package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;

class TicTacToeGameMain extends JFrame {
	
	// GUI for interactively playing the game
	public static TTTDisplayGame TTTdisplayGame;

  public static void main(String[] args) throws InterruptedException {
	// Create a program
	final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSTicTacToe.js");
	JFrame f = new TicTacToeGameMain();
	//f.setVisible(true);
	
	TTTdisplayGame = new TTTDisplayGame(bprog);

	BProgramRunner rnr = new BProgramRunner(bprog);
	rnr.addListener( new StreamLoggerListener() );
	rnr.start();
	
//	bprog.add(new UpdatePlayingGUI(), 0.1); 
//	bprog.add(new UserMove(), 0.5);
	 
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