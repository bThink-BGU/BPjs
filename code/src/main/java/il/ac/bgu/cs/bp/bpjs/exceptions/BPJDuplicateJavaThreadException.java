package il.ac.bgu.cs.bp.bpjs.exceptions;


public class BPJDuplicateJavaThreadException extends BPJException{

Thread existing;
	
	public BPJDuplicateJavaThreadException(Thread existing) {
		this.existing = existing;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BPJDuplicateJavaThreadException [name=" + existing +"]";
	}
	
	
}
