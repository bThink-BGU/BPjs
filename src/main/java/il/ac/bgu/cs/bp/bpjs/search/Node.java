package il.ac.bgu.cs.bp.bpjs.search;

import java.util.Iterator;
import java.util.Set;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.eventselection.SimpleEventSelectionStrategy;

/**
 * A single node in a program's execution tree. Contains the program's state, and
 * the last event to happen when getting to this state.
 * 
 * @author Gera
 * @author Reut
 * @author michael
 */
public class Node {
	
	private static final EventSelectionStrategy ess = new SimpleEventSelectionStrategy();
    
	private final BProgramSyncSnapshot systemState;
	private final BProgram bp;
	private final Set<BEvent> possibleEvents;
	private final BEvent lastEvent;
	private final Iterator<BEvent> iterator;

	protected Node(BProgram bp, BProgramSyncSnapshot systemState, BEvent e) {
		this.bp = bp;
		this.systemState = systemState;
		this.lastEvent = e;
	
		possibleEvents = ess.selectableEvents(systemState.getStatements(), systemState.getExternalEvents());
		iterator = possibleEvents.iterator();
	}

	private String stateString() {

		StringBuilder str = new StringBuilder();
        systemState.getBThreadSnapshots().forEach( s  -> 
            str.append("\t").append(s.toString()).append(" {").append(s.getBSyncStatement()).append("} \n")
        );

		return str.toString();
	}

	@Override
	public String toString() {
		return ((lastEvent != null) ? "\n\tevent: " + lastEvent + "\n" : "") + stateString();
	}

	public static Node getInitialNode(BProgram bp) throws Exception {
		BProgramSyncSnapshot seed = bp.setup().start();

		return new Node(bp, seed, null);
	}

	/**
	 * Get the events that can be triggered at the state.
	 * 
	 * @return The set of requested and not blocked events.
	 */
	public Set<BEvent> getPossibleEvents() {
		return possibleEvents;
	}

	/**
	 * Get a Node object for each possible state of the system after triggering
	 * the given event.
	 * 
	 * @param e
	 * @return
	 * @throws InterruptedException
	 */
	public Node getNextNode(BEvent e) throws Exception {
		return new Node(bp, BProgramSyncSnapshotCloner.clone(systemState).triggerEvent(e), e);
	}

	/**
	 * Check if this state is good or bad
	 * 
	 * @return True if the state is good.
	 */
	public boolean check() {
		return !possibleEvents.isEmpty();
	}

	/**
	 * Get the events that can be triggered at the state.
	 * 
	 * @return An iterator for the set of requested and not blocked events.
	 */
	public Iterator<BEvent> getEventIterator() {
		return iterator;
	}

    public BEvent getLastEvent() {
        return lastEvent;
    }

    public BProgramSyncSnapshot getSystemState() {
        return systemState;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((systemState == null) ? 0 : systemState.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Node)) return false;
        
		Node other = (Node) obj;
		if (systemState == null) {
			if (other.systemState != null) {
				return false;
            }
		} else if (!systemState.equals(other.systemState)) {
			return false;
        }
		return true;
	}
	
	

}
