package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.exceptions;

/**
 * Base class for exceptions thrown from a {@link BProgram}.
 * @author michael
 */
public class BProgramException extends RuntimeException {

    public BProgramException() {
    }

    public BProgramException(String message) {
        super(message);
    }

    public BProgramException(String message, Throwable cause) {
        super(message, cause);
    }

    public BProgramException(Throwable cause) {
        super(cause);
    }
    
}
