package il.ac.bgu.cs.bp.bpjs.exceptions;

@SuppressWarnings("serial")
public class BPJMissingBThreadException extends BPJException{

Thread thread;
	
	public BPJMissingBThreadException(Thread thread) {
		this.thread = thread;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BPJMissingBThreadException [JavaThread=" + thread +"]";
	}
	
	
}
