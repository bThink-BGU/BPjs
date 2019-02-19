package il.ac.bgu.cs.bp.bpjs.TicTacToe.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

/**
 * A base class for moves (X or O) in the tic-tac-toe game.
 */
@SuppressWarnings("serial")
public class Move extends BEvent {
	public int row;
	public int col;

	public Move(int row, int col, String type) {
		super(type + "(" + row + "," + col + ")");
		this.row = row;
		this.col = col;
	}

	/**
	 * A string to display on the board to represent the occurrence of this
	 * event.
	 */
	public String displayString() {
		return "[Move row:" + row + " col:" + col + "]"; 
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!getClass().isInstance(obj)) {
			return false;
		}
		Move other = (Move) obj;
		if (col != other.col) {
			return false;
		}
		return row == other.row;
	}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + this.row;
        hash = 23 * hash + this.col;
        return hash;
    }
    
}
