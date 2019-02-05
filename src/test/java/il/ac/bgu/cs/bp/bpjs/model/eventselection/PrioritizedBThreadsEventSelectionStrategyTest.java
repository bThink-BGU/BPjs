package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBThreadSyncSnapshot;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
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
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot("bt1", SyncStatement.make(bt("1")).request(Arrays.asList(EVT_1))),
            new MockBThreadSyncSnapshot("bt2", SyncStatement.make(bt("2")).request(Arrays.asList(EVT_2))),
            new MockBThreadSyncSnapshot("bt3", SyncStatement.make(bt("3")).request(Arrays.asList(EVT_3)))
        );
		
		assertEquals(new HashSet<>(Arrays.asList(EVT_3)), sut.selectableEvents(bpss));
	}
	
    @Test
	public void testSelectableEvents_noBlocking_double() throws InterruptedException {
		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot("bt1", SyncStatement.make(bt("1")).request(Arrays.asList(EVT_1))),
            new MockBThreadSyncSnapshot("bt2", SyncStatement.make(bt("2")).request(Arrays.asList(EVT_2))),
            new MockBThreadSyncSnapshot("bt2a", SyncStatement.make(bt("2a")).request(Arrays.asList(EVT_2A)))
        );
        
		
		assertEquals(new HashSet<>(Arrays.asList(EVT_2, EVT_2A)),
				sut.selectableEvents(bpss));
	}

	@Test	
	public void testSelectableEvents_withBlocking() {
		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot("bt1", SyncStatement.make(bt("1")).request(Arrays.asList(EVT_1))),
            new MockBThreadSyncSnapshot("bt2", SyncStatement.make(bt("2")).request(Arrays.asList(EVT_2)).block(EVT_3)),
            new MockBThreadSyncSnapshot("bt3", SyncStatement.make(bt("3")).request(Arrays.asList(EVT_3)))
        );
        
        assertEquals(new HashSet<>(Arrays.asList(EVT_2)),
				sut.selectableEvents(bpss));
	}
	
    @Test	
	public void testSelectableEvents_allBlocked() {
		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot("bt1", SyncStatement.make(bt("1")).request(Arrays.asList(EVT_1)).block(EVT_2)),
            new MockBThreadSyncSnapshot("bt2", SyncStatement.make(bt("2")).request(Arrays.asList(EVT_2)).block(EVT_3)),
            new MockBThreadSyncSnapshot("bt3", SyncStatement.make(bt("3")).request(Arrays.asList(EVT_3)).block(EVT_1))
        );
		
        assertEquals( Collections.emptySet(), sut.selectableEvents(bpss));
	}
	
    @Test	
	public void testSelectableEvents_withBlocking_double() {
		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot("bt1",  SyncStatement.make(bt("1")).request(Arrays.asList(EVT_1))),
            new MockBThreadSyncSnapshot("bt2",  SyncStatement.make(bt("2")).request(Arrays.asList(EVT_2))),
            new MockBThreadSyncSnapshot("bt2a", SyncStatement.make(bt("2a")).request(Arrays.asList(EVT_2A))),
            new MockBThreadSyncSnapshot("bt3",  SyncStatement.make(bt("3")).block(EVT_2A))
        );
        
        assertEquals(new HashSet<>(Arrays.asList(EVT_2)), sut.selectableEvents(bpss));
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
