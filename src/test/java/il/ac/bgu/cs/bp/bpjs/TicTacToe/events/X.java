package il.ac.bgu.cs.bp.bpjs.TicTacToe.events;

/**
 * An event that is fired when player X makes a move.
 */
@SuppressWarnings("serial")
public class X extends Move {

	/**
	 * Constructor.
	 */
	public X(int row, int col) {
		super(row, col);
		this.named("X(" + row + "," + col + ")");
		//this.setName("X(" + row + "," + col + ")");
	}

	/**
	 * @see tictactoe.events.Move#displayString()
	 */
	@Override
	public String displayString() {
		return "X";
	}

}
