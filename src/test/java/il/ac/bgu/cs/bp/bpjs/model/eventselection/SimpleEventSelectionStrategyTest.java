package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ExplicitEventSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class SimpleEventSelectionStrategyTest {
    
    private final BEvent eventOne = new BEvent("one");
    private final BEvent eventTwo = new BEvent("two");
    private final BEvent eventTri = new BEvent("tri");
    
    private SimpleEventSelectionStrategy sut;
    
    @Before
    public void setUp() {
        sut = new SimpleEventSelectionStrategy();
    }

    @Test
    public void testEmptySet() {
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS();
        assertEquals(Optional.empty(),
                sut.select(bpss, Collections.emptySet()) );
    }
    
    @Test
    public void testNoStatementsWithExternalEvents() {
        List<BEvent> externals = Arrays.asList(BEvent.named("a"), BEvent.named("b"));
        EventSelectionStrategy ess = new SimpleEventSelectionStrategy(700);
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS();
        bpss.getExternalEvents().addAll(externals);
        assertEquals(Collections.singleton(externals.get(0)),
                ess.selectableEvents(bpss) );
    }
    
    @Test
    public void testUnanimousCase() {
        BEvent expected = eventOne;
        
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
                new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventOne)),
                new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventOne).waitFor(eventTwo)),
                new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventOne))
        );
        assertEquals(singleton(expected), sut.selectableEvents(bpss));
        assertEquals(new EventSelectionResult(expected),
                                  sut.select(bpss,singleton(eventOne)).get());
    }
    
    @Test
    public void testWithBlockingCase() {
        BEvent expected = eventOne;
        
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventOne)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventTwo)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).block(eventTwo))
        );
        assertEquals(singleton(expected), sut.selectableEvents(bpss));
        assertEquals(new EventSelectionResult(expected),
                           sut.select(bpss, singleton(expected)).get());
    }
    
    @Test
    public void testDeadlock() {
        ExplicitEventSet setOfEvt2 = new ExplicitEventSet();
        setOfEvt2.add(eventTwo);
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventOne)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).request(setOfEvt2)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).block(eventTwo)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).block(eventOne))
        );
        assertEquals(emptySet(), sut.selectableEvents(bpss));
        assertEquals(Optional.empty(), sut.select(bpss, emptySet()));
    }
    
    @Test
    public void testNoRequests() {
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make(null).waitFor(eventOne)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).waitFor(eventTwo)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).block(eventTwo)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).block(eventOne))
        );
        assertEquals(emptySet(), sut.selectableEvents(bpss));
        assertEquals(Optional.empty(), sut.select(bpss, emptySet()));
    }
    
    @Test
    public void testDeadlockWithExternals() {
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventOne)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).request(eventTwo)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).block(eventTwo)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).block(eventOne))
        );
        List<BEvent> externals = Arrays.asList(eventOne, eventTri, eventTri, eventTwo);
        bpss.getExternalEvents().addAll(externals);
        assertEquals( singleton(eventTri), sut.selectableEvents(bpss));
        assertEquals( new EventSelectionResult(eventTri, singleton(1)), 
                           sut.select(bpss, singleton(eventTri)).get());
    }
    
    @Test
    public void testNoInternalRequests() {
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make(null).waitFor(eventOne)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).waitFor(eventTri)),
            new MockBThreadSyncSnapshot(SyncStatement.make(null).waitFor(eventTwo))
        );
        List<BEvent> externals = Arrays.asList(eventOne, eventTwo, eventTri, eventTwo);
        bpss.getExternalEvents().addAll(externals);
        
        assertEquals( singleton(eventOne), sut.selectableEvents(bpss));
        assertEquals( new EventSelectionResult(eventOne, singleton(0)),
                      sut.select(bpss, singleton(eventOne)).get());
    }
    
    @Test
    public void testSeed() {
        Set<BEvent> events = IntStream.range(0, 1000).mapToObj( i -> BEvent.named("evt"+i) ).collect( toSet() );
        Set<BThreadSyncSnapshot> btssSet = new HashSet<>();
        events.stream().map(e -> SyncStatement.make().request(e))
                       .map( s->new MockBThreadSyncSnapshot(s))
                       .forEach(btssSet::add);
        BProgramSyncSnapshot bpss = TestUtils.makeBPSS(btssSet);
        
        // See what sut does
        List<BEvent> selectedBySut = new ArrayList<>(1000);
        for ( int i=0; i<1000; i++ ) {
            selectedBySut.add( sut.select(bpss, events).get().getEvent() );
        }
        
        // replicate with a strategy with the same seed
        EventSelectionStrategy sameSeedEss = new SimpleEventSelectionStrategy(sut.getSeed());
        List<BEvent> selectedBySameSeed = new ArrayList<>(1000);
        for ( int i=0; i<1000; i++ ) {
            selectedBySameSeed.add( sameSeedEss.select(bpss, events).get().getEvent() );
        }
        
        assertEquals( selectedBySut, selectedBySameSeed );
    }
}
