package il.ac.bgu.cs.bp.bpjs.examples.analysis.TicTacToe.events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

/**
 * An event that is requested (with high priority) whenever the user presses a
 * button on the game board.
 */
@SuppressWarnings("serial")
public class Click extends BEvent {

	/**
	 * Row of the pressed button
	 */
	public int row;

	/**
	 * Column of the pressed button
	 */
	public int col;

	/**
	 * Constructor.
	 * 
	 * @param row
	 *            Row of the pressed button
	 * @param col
	 *            Column of the pressed button
	 */
	public Click(int row, int col) {
		super("Click(" + row + "," + col + ")");
		this.row = row;
		this.col = col;
	}

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + this.row;
        hash = 11 * hash + this.col;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Click other = (Click) obj;
        if (this.row != other.row) {
            return false;
        }
        return this.col == other.col;
    }

    
    
}
