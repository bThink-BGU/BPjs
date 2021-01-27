package il.ac.bgu.cs.bp.bpjs.examples.analysis.TicTacToe.events;

/**
 * An event that is fired when player X makes a move.
 */
@SuppressWarnings("serial")
public class X extends Move {

	public X(int row, int col) {
		super(row, col, "X");
	}

	@Override
	public String displayString() {
		return "X";
	}
    
      @Override
   public boolean equals( Object other ) {
       if ( other == this ) return true;
       if ( other == null ) return false;
       if ( other instanceof X ) { 
           return super.equals(other);
       } else return false;
   }
   
   @Override
   public int hashCode() { 
       return super.hashCode();
   }

}
