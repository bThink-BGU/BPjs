package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.List;

@SuppressWarnings("serial")
public class BadTraceException extends Exception {
	private List<Node> badTrace;

	public BadTraceException(List<Node> badTrace) {
		super();
		this.badTrace = badTrace;
	}

	public List<Node> getBadTrace() {
		return badTrace;
	}
	
}
