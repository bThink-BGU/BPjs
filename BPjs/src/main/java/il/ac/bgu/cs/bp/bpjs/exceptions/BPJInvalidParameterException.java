package il.ac.bgu.cs.bp.bpjs.exceptions;

public class BPJInvalidParameterException extends RuntimeException {
	String description;

	public BPJInvalidParameterException(String description) {
		super();
		this.description = description;
	}

	@Override
	public String toString() {
		return "BPJInavlidParameterException" + description ;
	}

}
