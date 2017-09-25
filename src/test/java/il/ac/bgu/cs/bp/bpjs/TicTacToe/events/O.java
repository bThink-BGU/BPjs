package il.ac.bgu.cs.bp.bpjs.TicTacToe.events;

/**
 * An event that is executed when player O makes a move.
 */
@SuppressWarnings("serial")
public class O extends Move {
	
	public int priority = 1;
	
	/**
	 * Constructor.
	 */
	public O(int row, int col) {
		super(row, col, "O");
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

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		if(priority>0 && priority<4)
			this.priority = priority;
	}

}
