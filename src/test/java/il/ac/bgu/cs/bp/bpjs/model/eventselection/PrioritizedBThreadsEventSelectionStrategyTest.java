package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.junit.Before;

public class PrioritizedBThreadsEventSelectionStrategyTest {

	private static final BEvent evt1 = new BEvent("evt1");
	private static final BEvent evt2 = new BEvent("evt2");
	private static final BEvent evt2a = new BEvent("evt2a");
	private static final BEvent evt3 = new BEvent("evt3");
    
	PrioritizedBThreadsEventSelectionStrategy sut;
    
    @Before
    public void setup() {
        sut = new PrioritizedBThreadsEventSelectionStrategy();
        sut.setPriority("bt1", 1);
        sut.setPriority("bt2", 2);
        sut.setPriority("bt2a", 2);
        sut.setPriority("bt3", 3);
    }
    
	@Test
	public void testSelectableEvents_noBlocking() throws InterruptedException {
		Set<BSyncStatement> stmts = new HashSet<>();
		stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(evt1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(evt2)));
		stmts.add(BSyncStatement.make(bt("3")).request(Arrays.asList(evt3)));
		
		assertEquals(new HashSet<>(Arrays.asList(evt3)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}
	
    @Test
	public void testSelectableEvents_noBlocking_double() throws InterruptedException {
		Set<BSyncStatement> stmts = new HashSet<>();
		stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(evt1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(evt2)));
		stmts.add(BSyncStatement.make(bt("2a")).request(Arrays.asList(evt2a)));
		
		assertEquals(new HashSet<>(Arrays.asList(evt2, evt2a)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}

	@Test	
	public void testSelectableEvents_withBlocking() {
		Set<BSyncStatement> stmts = new HashSet<>();
        
        stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(evt1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(evt2)).block(evt3));
		stmts.add(BSyncStatement.make(bt("3")).request(Arrays.asList(evt3)));
		
        
        assertEquals(new HashSet<>(Arrays.asList(evt2)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}
	
    @Test	
	public void testSelectableEvents_withBlocking_double() {
		Set<BSyncStatement> stmts = new HashSet<>();
        
        stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(evt1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(evt2)));
		stmts.add(BSyncStatement.make(bt("2a")).request(Arrays.asList(evt2a)));
		stmts.add(BSyncStatement.make(bt("3")).block(evt2a));
        
        assertEquals(new HashSet<>(Arrays.asList(evt2)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}
    
    @Test
    public void testPriorityNumbers() { 
        assertEquals( 1, sut.getPriority("bt1") );
        assertEquals( 2, sut.getPriority("bt2") );
        assertEquals( PrioritizedBThreadsEventSelectionStrategy.DEFAULT_PRIORITY,
                            sut.getPriority("was-not-registered") );
        
        assertEquals( 3, sut.getHighestPriority() );
        assertEquals( 1, sut.getLowestPriority() );
    }
    
    private BThreadSyncSnapshot bt( String name ) {
        return new BThreadSyncSnapshot("bt"+name, null);
    }

}
