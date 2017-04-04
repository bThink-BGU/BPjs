package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.Set;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.eventselection.SimpleEventSelectionStrategy;

public class Node {
	private BProgramSyncSnapshot systemState;

	private BProgram bp;

	private Set<BEvent> possibleEvents;
	private static EventSelectionStrategy ess = new SimpleEventSelectionStrategy();

	private Node(BProgram bp, BProgramSyncSnapshot systemState) {
		this.bp = bp;
		this.systemState = systemState;
		
		possibleEvents = ess.selectableEvents(systemState.getStatements(), systemState.getExternalEvents());
	}

	@Override
	public String toString() {
		String str = "\n";
		for (BThreadSyncSnapshot s : systemState.getBThreadSnapshots()) {
			str += "\t" + s.toString() + " {"+ s.getBSyncStatement() +  "} \n";
		}
		return str;
	}

	public static Node getInitialNode(BProgram bp) throws Exception {
		return new Node(bp, bp.setup().start());
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
	 * Get all a Node object for each possible state of the system after
	 * triggering the given event.
	 * 
	 * @param e
	 * @return
	 * @throws InterruptedException 
	 */
	public Node getNextNode(BEvent e) throws Exception {
		return new Node(bp, systemState.clone().triggerEvent(e));
	}

	/**
	 * Check if this state is good or bad
	 * 
	 * @return True if the state is good.
	 */
	public boolean check() {
		return possibleEvents.isEmpty();
	}

}
