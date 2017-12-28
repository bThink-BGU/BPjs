package il.ac.bgu.cs.bp.bpjs.analysis.eventpattern;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

/**
 * Part of an {@link EventPattern}.
 * 
 * @author michael
 */
public class EventPatternPart  {
	
	private final EventSet eventSet;
	private final int minRepeats;
	private final Integer maxRepeats;
	

    public EventPatternPart(EventSet eventSet, int minRepeats, Integer maxRepeats) {
		super();
		this.eventSet = eventSet;
		this.minRepeats = minRepeats;
		this.maxRepeats = maxRepeats;
	}

	public boolean matches( BEvent e ) {
		return eventSet.contains(e);
	}
	
	public int getMinRepeats() {
		return minRepeats;
	}

	public Integer getMaxRepeats() {
		return maxRepeats;
	}

	public EventSet getEventSet() {
		return eventSet;
	}
	
	public boolean isMultiChar() {
		return ( minRepeats!=1 || (maxRepeats!=null && maxRepeats!=1) );
	}

	@Override
	public String toString() {
		return "[EventPatternPart eventSet:" + eventSet + ", minRepeats:"
				+ minRepeats + ", maxRepeats:" + maxRepeats + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((eventSet == null) ? 0 : eventSet.hashCode());
		result = prime * result
				+ ((maxRepeats == null) ? 0 : maxRepeats.hashCode());
		result = prime * result + minRepeats;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EventPatternPart other = (EventPatternPart) obj;
		if (eventSet == null) {
			if (other.eventSet != null)
				return false;
		} else if (!eventSet.equals(other.eventSet))
			return false;
		if (maxRepeats == null) {
			if (other.maxRepeats != null)
				return false;
		} else if (!maxRepeats.equals(other.maxRepeats))
			return false;
		return minRepeats == other.minRepeats;
	}
	
}
