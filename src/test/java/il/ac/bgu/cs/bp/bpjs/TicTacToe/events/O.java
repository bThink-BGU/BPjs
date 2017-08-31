package il.ac.bgu.cs.bp.bpjs.TicTacToe.events;

/**
 * An event that is executed when player O makes a move.
 */
@SuppressWarnings("serial")
public class O extends Move {
	/**
	 * Constructor.
	 */
	public O(int row, int col) {
		super(row, col);
		this.named("O(" + row + "," + col + ")");
		//this.setName("O(" + row + "," + col + ")");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see tictactoe.events.Move#displayString()
	 */
	@Override
	public String displayString() {
		return "O";
	}

}
