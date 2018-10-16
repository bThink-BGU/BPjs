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

	private static final BEvent EVT_1 = new BEvent("evt1");
	private static final BEvent EVT_2 = new BEvent("evt2");
	private static final BEvent EVT_2A = new BEvent("evt2a");
	private static final BEvent EVT_3 = new BEvent("evt3");
    
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
		stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(EVT_1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(EVT_2)));
		stmts.add(BSyncStatement.make(bt("3")).request(Arrays.asList(EVT_3)));
		
		assertEquals(new HashSet<>(Arrays.asList(EVT_3)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}
	
    @Test
	public void testSelectableEvents_noBlocking_double() throws InterruptedException {
		Set<BSyncStatement> stmts = new HashSet<>();
		stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(EVT_1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(EVT_2)));
		stmts.add(BSyncStatement.make(bt("2a")).request(Arrays.asList(EVT_2A)));
		
		assertEquals(new HashSet<>(Arrays.asList(EVT_2, EVT_2A)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}

	@Test	
	public void testSelectableEvents_withBlocking() {
		Set<BSyncStatement> stmts = new HashSet<>();
        
        stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(EVT_1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(EVT_2)).block(EVT_3));
		stmts.add(BSyncStatement.make(bt("3")).request(Arrays.asList(EVT_3)));
		
        
        assertEquals(new HashSet<>(Arrays.asList(EVT_2)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}
	
    @Test	
	public void testSelectableEvents_withBlocking_double() {
		Set<BSyncStatement> stmts = new HashSet<>();
        
        stmts.add(BSyncStatement.make(bt("1")).request(Arrays.asList(EVT_1)));
		stmts.add(BSyncStatement.make(bt("2")).request(Arrays.asList(EVT_2)));
		stmts.add(BSyncStatement.make(bt("2a")).request(Arrays.asList(EVT_2A)));
		stmts.add(BSyncStatement.make(bt("3")).block(EVT_2A));
        
        assertEquals(new HashSet<>(Arrays.asList(EVT_2)),
				sut.selectableEvents(stmts, Collections.emptyList()));
	}
    
    @Test
    public void testPriorityNumbers() { 
        assertEquals( 1, sut.getPriority("bt1") );
        assertEquals( 2, sut.getPriority("bt2") );
        assertEquals( 2, sut.getPriority("bt2a") );
        assertEquals( 3, sut.getPriority("bt3") );
        
        assertEquals( PrioritizedBThreadsEventSelectionStrategy.DEFAULT_PRIORITY,
                            sut.getPriority("was-not-registered") );
        
        assertEquals( 3, sut.getHighestPriority() );
        assertEquals( 1, sut.getLowestPriority() );
    }
    
    private BThreadSyncSnapshot bt( String name ) {
        return new BThreadSyncSnapshot("bt"+name, null);
    }

}
