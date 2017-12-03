package il.ac.bgu.cs.bp.bpjs.eventselection;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;

//@Ignore
public class PrioritizedBThreadsEventSelectionStrategyTest {

	private static final BEvent evt1 = new BEvent("evt1");
	private static final BEvent evt2 = new BEvent("evt2");
	private static final BEvent evt3 = new BEvent("evt3");
	private static final BEvent evt4 = new BEvent("evt4");
	
	@Test
	public void testSelectableEvents_noBlocking() throws InterruptedException {
		PrioritizedBThreadsEventSelectionStrategy sut = new PrioritizedBThreadsEventSelectionStrategy();

		Set<BSyncStatement> stmts = new HashSet<>();
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt4)));
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt1)).data(5));
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt2)).data(10));
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt3)).data(10));
		
		assertEquals(new HashSet<>(Arrays.asList(evt2, evt3)), //
				sut.selectableEvents(stmts, Collections.emptyList()));
	}

	@Test	
	public void testSelectableEvents_withBlocking() {
		PrioritizedBThreadsEventSelectionStrategy sut = new PrioritizedBThreadsEventSelectionStrategy();

		Set<BSyncStatement> stmts = new HashSet<>();
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt4)));
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt1)).data(5));
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt2)).data(10));
		stmts.add(BSyncStatement.make().request(Arrays.asList(evt3)).data(10).block(evt2));

		assertEquals(new HashSet<>(Arrays.asList(evt3)), //
				sut.selectableEvents(stmts, Collections.emptyList()));
	}

}
