package il.ac.bgu.cs.bp.bpjs.diningphil;

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

	private BEvent lastEvent;
	private static EventSelectionStrategy ess = new SimpleEventSelectionStrategy();

	protected Node(BProgram bp, BProgramSyncSnapshot systemState, BEvent e) {
		this.bp = bp;
		this.systemState = systemState;
		this.lastEvent = e;

		possibleEvents = ess.selectableEvents(systemState.getStatements(), systemState.getExternalEvents());
	}

	@Override
	public String toString() {
		String str = "\n";
		for (BThreadSyncSnapshot s : systemState.getBThreadSnapshots()) {
			str += "\t" + s.toString() + " {" + s.getBSyncStatement() + "} \n";
		}
		
		return ((lastEvent!= null) ? "\t\nevent: "+lastEvent + "\n" : "") + str;
	}

	public static Node getInitialNode(BProgram bp) throws Exception {
		return new Node(bp, bp.setup().start(),null);
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
	 * Get a Node object for each possible state of the system after
	 * triggering the given event.
	 * 
	 * @param e
	 * @return
	 * @throws InterruptedException
	 */
	public Node getNextNode(BEvent e) throws Exception {
		return new Node(bp, new BProgramSyncSnapshotCloner().clone(systemState).triggerEvent(e),e);
	}

	/**
	 * Check if this state is good or bad
	 * 
	 * @return True if the state is good.
	 */
	public boolean check() {
		return !possibleEvents.isEmpty();
	}

}
