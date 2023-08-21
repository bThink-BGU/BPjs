package il.ac.bgu.cs.bp.bpjs.exceptions;

import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;

public class BPjsOutsideOfBThreadException extends BPjsRuntimeException {

    public BPjsOutsideOfBThreadException(String message) {
        super(message);
    }

    public BPjsOutsideOfBThreadException(String message, Throwable cause) {
        super(message, cause);
    }
}
