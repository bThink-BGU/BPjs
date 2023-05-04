package il.ac.bgu.cs.bp.bpjs.exceptions;

/**
 * Thrown when a sync statement is called outside of a BThread.
 *
 * @author maor
 */
public class BPjsSyncOutsideBThreadException extends BPjsRuntimeException{

    public BPjsSyncOutsideBThreadException(String message) {
        super(message);
    }

    public BPjsSyncOutsideBThreadException(String message, Throwable cause) {
        super(message, cause);
    }
}
