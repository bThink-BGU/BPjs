package il.ac.bgu.cs.bp.bpjs.exceptions;


/**
 * Base class for exceptions thrown from BPjs code.
 * @author michael
 */
public abstract class BPjsException extends RuntimeException {
    
    public BPjsException( String message ) {
        super(message);
    }
    
    public BPjsException( String message, Throwable cause ) {
        super( message, cause );
    }
	
}

