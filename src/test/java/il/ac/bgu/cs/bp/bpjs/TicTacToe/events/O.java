package il.ac.bgu.cs.bp.bpjs.TicTacToe.events;

/**
 * An event that is executed when player O makes a move.
 */
@SuppressWarnings("serial")
public class O extends Move {
	
	int priority;
	
	public O(int row, int col) {
		this(row,col,0);
	}
	
	public O(int row, int col, int priority) {
		super(row, col, "O");
		this.priority=priority;
	}

	@Override
	public String displayString() {
		return "O";
	}

	public void priority(int p) {
		priority=p;
	}
    
   @Override
   public boolean equals( Object other ) {
       if ( other == this ) return true;
       if ( other == null ) return false;
       if ( other instanceof O ) { 
           O otherO = (O) other;
           return otherO.priority == this.priority && super.equals(other);
       } else return false;
   }
   
   @Override
   public int hashCode() { 
       return super.hashCode()*(priority+1);
   }
}
