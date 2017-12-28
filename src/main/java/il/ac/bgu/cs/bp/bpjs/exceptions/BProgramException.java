package il.ac.bgu.cs.bp.bpjs.exceptions;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;

/**
 * Base class for exceptions thrown from a {@link BProgram}.
 * @author michael
 */
public class BProgramException extends RuntimeException {

    public BProgramException(String message) {
        super(message);
    }

    public BProgramException(String message, Throwable cause) {
        super(message, cause);
    }

}
