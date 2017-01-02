package il.ac.bgu.cs.bp.bpjs.exceptions;


@SuppressWarnings("serial")
public class BPJException extends RuntimeException {

	public BPJException() {
		super();
	}

	public BPJException(String s) {
		super(s);
	}

	public BPJException(Throwable cause) {
		super(cause);
	}
}
