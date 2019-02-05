package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBThreadSyncSnapshot;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

public class PrioritizedBSyncEventSelectionStrategyTest {

	private static final BEvent EVT_1 = new BEvent("evt1");
	private static final BEvent EVT_2 = new BEvent("evt2");
	private static final BEvent EVT_3 = new BEvent("evt3");
	private static final BEvent EVT_4 = new BEvent("evt4");
	
	@Test
	public void testSelectableEvents_noBlocking() throws InterruptedException {
		PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy();

		BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_4))),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1)).data(5)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2)).data(10)),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_3)).data(10))
        );
		assertEquals(new HashSet<>(Arrays.asList(EVT_2, EVT_3)), sut.selectableEvents(bpss));
	}

	@Test
	public void testSelectableEvents_withBlocking() {
		PrioritizedBSyncEventSelectionStrategy sut = new PrioritizedBSyncEventSelectionStrategy();

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

}
