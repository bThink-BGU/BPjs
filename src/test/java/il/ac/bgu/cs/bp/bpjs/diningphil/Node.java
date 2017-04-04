package il.ac.bgu.cs.bp.bpjs.diningphil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.BProgramListener;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionStrategy;

public class Node {
	private Set<BThreadSyncSnapshot> system_state;

	private EventSelectionStrategy eventSelectionStrategy;
	private BProgramSyncSnapshot cur;
	private final List<BProgramListener> listeners = new ArrayList<>();
	private Optional<EventSelectionResult> res = null;
	private BProgram bp;
	
	Node(){}
	
	Node(BProgram bp){
		this.bp = bp;
	}
	
	@Override
	public String toString() {
		String str = "";
		Object[] tmp = system_state.toArray();
		for (int i = 0; i < system_state.size(); i++) {
			str += tmp[i].toString() + '\n';
		}
		return str;
	}

	public void addBTSS(BThreadSyncSnapshot BTSS)
	{
		system_state.add(BTSS);
	}
	
	//problem with static
	public static Node getInitialNode(BProgram bp) {
		//BProgramSyncSnapshot cur = bp.setup();
		listeners.forEach(l -> l.starting(bp));
		Node n = new Node();
		n.addBTSS((BThreadSyncSnapshot) cur.getBThreadSnapshots());
		return n;
	}
	
	/**
	 * Get the events that can be triggered at the state.
	 * 
	 * @return The set of requested and not blocked events.
	 */
	public Set<BEvent> getPossibleEvents() {
		//EventSelectionStrategy eventSelectionStrategy;
		Set<BEvent> possibleEvents = eventSelectionStrategy.selectableEvents(cur.getStatements(), cur.getExternalEvents());
		return possibleEvents;
	}

	/**
	 * Get all a Node object for each possible state of the system after
	 * triggering the given event.
	 * 
	 * @param e
	 * @return
	 */
	public Node getNextNode(BEvent e) {
		//Optional<EventSelectionResult> res = eventSelectionStrategy.select(cur.getStatements(), cur.getExternalEvents(), getPossibleEvents());
		res = eventSelectionStrategy.select(cur.getStatements(), cur.getExternalEvents(), getPossibleEvents());
		if ( res.isPresent() ) {
			EventSelectionResult esr = res.get();
			listeners.forEach(l->l.eventSelected(bp, esr.getEvent())); 
			try {
				cur = cur.triggerEvent(esr.getEvent(), listeners);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Check if this state is good or bad
	 * 
	 * @return True if the state is good.
	 */
	public boolean check() {
		return res == null;

		//return false;
	}

}

