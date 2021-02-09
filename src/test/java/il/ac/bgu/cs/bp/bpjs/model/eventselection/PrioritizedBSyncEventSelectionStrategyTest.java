package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBThreadSyncSnapshot;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import java.util.Set;
import static org.junit.Assert.assertTrue;

public class PrioritizedBSyncEventSelectionStrategyTest {

	private static final BEvent EVT_1 = new BEvent("evt1");
	private static final BEvent EVT_2 = new BEvent("evt2");
	private static final BEvent EVT_3 = new BEvent("evt3");
	private static final BEvent EVT_4 = new BEvent("evt4");
	
	@Test
	public void testSelectableEvents_noBlocking() throws InterruptedException {
		PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy(1224l);

		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_4))),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1)).data(5)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2)).data(10)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_3)).data(10))
        );
		assertEquals(new HashSet<>(Arrays.asList(EVT_2, EVT_3)), sut.selectableEvents(bpss));
	}
    
    @Test
	public void testSelectableEvents_changedPriority() throws InterruptedException {
		PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy();
        sut.setDefaultPriority(11);
        
		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_4))),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1)).data(5)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2)).data(10)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_3)).data(10))
        );
		assertEquals(new HashSet<>(Arrays.asList(EVT_4)), sut.selectableEvents(bpss));
		assertEquals(11, sut.getDefaultPriority());
	}

	@Test
	public void testSelectableEvents_withBlocking() {
		PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy(500);

		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_4))),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1)).data(5)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2)).data(1)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_3)).data(10).block(EVT_2))
        );
		assertEquals(new HashSet<>(Arrays.asList(EVT_3)), sut.selectableEvents(bpss));
	}
    
    @Test
	public void testSelectableEvents_allBlocked() {
		PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy();

		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_4)).block(EVT_1)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1)).data(5).block(EVT_2)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2)).data(1).block(EVT_3)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_3)).data(10).block(EVT_4))
        );

		assertEquals(Collections.emptySet(), sut.selectableEvents(bpss));
	}
    
    @Test
	public void testSelectableEvents_allBlockedWithExtrnal() {
		
        BEvent external = new BEvent("External");
        PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy();
           
		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_4)).block(EVT_1)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1)).data(5).block(EVT_2)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2)).data(1).block(EVT_3)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_3)).data(10).block(EVT_4))
        );
        bpss.getExternalEvents().add(external);

		assertEquals(Collections.singleton(external), sut.selectableEvents(bpss));
	}
    
    @Test
	public void testSCornerCases() {
		PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy(500);

		BProgramSyncSnapshot bpss = TestUtils.makeBPSS();
        assertTrue(sut.selectableEvents(bpss).isEmpty());
        bpss.getExternalEvents().add(EVT_1);
        assertEquals( Set.of(EVT_1), sut.selectableEvents(bpss) );
	}
}
