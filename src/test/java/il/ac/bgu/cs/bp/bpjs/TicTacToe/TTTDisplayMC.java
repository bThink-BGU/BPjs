package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;

/**
 * Class that implements the display of the game
 */
public class TTTDisplayMC {
	private BProgram bp;
	private JFrame window = new JFrame("Tic-Tac-Toe");
	public JButton buttons[][] = new JButton[3][];
	public JLabel message = new JLabel();

	/**
	 * Constructor.
	 */

	public TTTDisplayMC(BProgram BP) {

		bp = BP;
		
		// Create window
		window.setSize(150, 150);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());
		window.setLocation(new Point(550,100)); 

		// The board
		JPanel board = new JPanel();
		board.setLayout(new GridLayout(3, 3));
		// The message label
		message.setHorizontalAlignment(SwingConstants.CENTER);

		// Create buttons
		for (int i = 0; i < 3; i++) {
			buttons[i] = new JButton[3];
			for (int j = 0; j < 3; j++) {
				buttons[i][j] = new TTTButton(i, j);
				board.add(buttons[i][j]);
				//				buttons[i][j].addActionListener(this);
				buttons[i][j].setEnabled(false);
			}
		}

		// Add the board and the message component to the window
		window.add(board, BorderLayout.CENTER);
		window.add(message, BorderLayout.SOUTH);

		// Make the window visible
		window.setVisible(true);
	}

	/**
	 * A button that remembers its position on the board
	 */
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
}