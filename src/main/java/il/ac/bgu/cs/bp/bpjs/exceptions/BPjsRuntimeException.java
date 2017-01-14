package il.ac.bgu.cs.bp.bpjs.exceptions;

/**
 * An exception thrown when a code "makes no sense", e.g. when client code has
 * tries to make an invalid event set.
 * 
 * @author michael
 */
public class BPjsRuntimeException extends BPjsException {

    public BPjsRuntimeException(String message) {
        super(message);
    }

    public BPjsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
