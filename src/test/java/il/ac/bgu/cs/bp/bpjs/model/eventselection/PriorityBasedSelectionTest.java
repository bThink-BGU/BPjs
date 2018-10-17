/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

/**
 *
 * @author michael
 */
public class PriorityBasedSelectionTest {

	BProgram prog;
	Map<String, BEvent> events = new HashMap<>();
	Map<String, EventSet> eventSets = new HashMap<>();

	@Test
	public void testBadPredicate()  {
		try {

			StringBProgram bProgram = new StringBProgram("" //
					+ "bp.registerBThread('bt1',function(){" //
					+ "  bp.sync({request:bp.Event('X')});"//
					+ "});"//
					+ "bp.registerBThread('bt2',function(){"//
					+ "  bp.sync({request:bp.Event('Y')});"//
					+ "});"//
			);
			
			PrioritizedBThreadsEventSelectionStrategy eventSelectionStrategy = new PrioritizedBThreadsEventSelectionStrategy();
			eventSelectionStrategy.setPriority("bt1", 1);

			bProgram.setEventSelectionStrategy(eventSelectionStrategy);

			BProgramRunner bpr = new BProgramRunner(bProgram);
			bpr.run();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
