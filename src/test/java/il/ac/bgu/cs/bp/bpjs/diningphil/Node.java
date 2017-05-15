package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.Iterator;
import java.util.Set;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.eventselection.SimpleEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.search.BProgramSyncSnapshotCloner;

public class Node {
	private BProgramSyncSnapshot systemState;

	private BProgram bp;

	private Set<BEvent> possibleEvents;
	// private BProgramSyncSnapshot seed;
	private BEvent lastEvent;
	private static EventSelectionStrategy ess = new SimpleEventSelectionStrategy();

	private Iterator<BEvent> iterator;

	protected Node(BProgram bp, BProgramSyncSnapshot systemState, BEvent e) {
		this.bp = bp;
		this.systemState = systemState;
		this.lastEvent = e;

		possibleEvents = ess.selectableEvents(systemState.getStatements(), systemState.getExternalEvents());
		iterator = possibleEvents.iterator();
	}

	private String stateString() {

		StringBuilder sb = new StringBuilder();
		for (BThreadSyncSnapshot s : systemState.getBThreadSnapshots()) {
			sb.append("\t").append(s.toString()).append(" {").append(s.getBSyncStatement()).append("} \n");
		}

		return sb.toString();
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

	@Override
	public int hashCode() {
		return stateString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Node)
			return ((Node) obj).stateString().equals(stateString());
		else
			return false;
	}

}
