package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ExplicitEventSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.emptyList;
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
        assertEquals(Optional.empty(),
                sut.select(Collections.emptySet(), Collections.emptyList(), Collections.emptySet()) );
    }
    
    @Test
    public void testNoStatementsWithExternalEvents() {
        List<BEvent> externals = Arrays.asList(BEvent.named("a"), BEvent.named("b"));
        EventSelectionStrategy ess = new SimpleEventSelectionStrategy(700);
        assertEquals(Collections.singleton(externals.get(0)),
                ess.selectableEvents(Collections.emptySet(), externals) );
    }
    
    @Test
    public void testUnanimousCase() {
        BEvent expected = eventOne;
        
        Set<BSyncStatement> sets = new HashSet<>(Arrays.asList(
                BSyncStatement.make(null).request(eventOne),
                BSyncStatement.make(null).request(eventOne).waitFor(eventTwo),
                BSyncStatement.make(null).request(eventOne)
        ));
        assertEquals(singleton(expected), sut.selectableEvents(sets, Collections.emptyList()));
        assertEquals(Optional.of(new EventSelectionResult(expected)), sut.select(sets, Collections.emptyList(),singleton(eventOne)));
    }
    
    @Test
    public void testWithBlockingCase() {
        BEvent expected = eventOne;
        
        Set<BSyncStatement> sets = new HashSet<>(Arrays.asList(BSyncStatement.make(null).request(eventOne),
                BSyncStatement.make(null).request(eventTwo),
                BSyncStatement.make(null).block(eventTwo)
        ));
        assertEquals(singleton(expected), sut.selectableEvents(sets, Collections.emptyList()));
        assertEquals(Optional.of(new EventSelectionResult(expected)),
                           sut.select(sets, Collections.emptyList(), singleton(expected)));
    }
    
    @Test
    public void testDeadlock() {
        ExplicitEventSet setOfEvt2 = new ExplicitEventSet();
        setOfEvt2.add(eventTwo);
        Set<BSyncStatement> sets = new HashSet<>(Arrays.asList(
                BSyncStatement.make(null).request(eventOne),
                BSyncStatement.make(null).request(setOfEvt2),
                BSyncStatement.make(null).block(eventTwo),
                BSyncStatement.make(null).block(eventOne)
        ));
        assertEquals(emptySet(), sut.selectableEvents(sets, Collections.emptyList()));
        assertEquals(Optional.empty(), sut.select(sets, Collections.emptyList(), emptySet()));
    }
    
    @Test
    public void testNoRequests() {
        Set<BSyncStatement> sets = new HashSet<>(Arrays.asList(
                BSyncStatement.make(null).waitFor(eventOne),
                BSyncStatement.make(null).waitFor(eventTwo),
                BSyncStatement.make(null).block(eventTwo),
                BSyncStatement.make(null).block(eventOne)
        ));
        assertEquals(emptySet(), sut.selectableEvents(sets, Collections.emptyList()));
        assertEquals(Optional.empty(), sut.select(sets, Collections.emptyList(), emptySet()));
    }
    
    @Test
    public void testDeadlockWithExternals() {
        Set<BSyncStatement> sets = new HashSet<>(Arrays.asList(
                BSyncStatement.make(null).request(eventOne),
                BSyncStatement.make(null).request(eventTwo),
                BSyncStatement.make(null).block(eventTwo),
                BSyncStatement.make(null).block(eventOne)
        ));
        List<BEvent> externals = Arrays.asList(eventOne, eventTri, eventTri, eventTwo);
        assertEquals( singleton(eventTri), sut.selectableEvents(sets, externals));
        assertEquals( Optional.of(new EventSelectionResult(eventTri, singleton(1))), 
                           sut.select(sets, externals, singleton(eventTri)));
    }
    
    @Test
    public void testNoInternalRequests() {
        Set<BSyncStatement> sets = new HashSet<>(Arrays.asList(
                BSyncStatement.make(null).waitFor(eventOne),
                BSyncStatement.make(null).waitFor(eventTri),
                BSyncStatement.make(null).waitFor(eventTwo)
        ));
        List<BEvent> externals = Arrays.asList(eventOne, eventTwo, eventTri, eventTwo);
        
        assertEquals( singleton(eventOne), sut.selectableEvents(sets, externals));
        assertEquals(Optional.of( new EventSelectionResult(eventOne, singleton(0))),
                      sut.select(sets, externals, singleton(eventOne)));
    }
    
    @Test
    public void testSeed() {
        Set<BEvent> events = IntStream.range(0, 1000).mapToObj( i -> BEvent.named("evt"+i) ).collect( toSet() );
        Set<BSyncStatement> stmts = events.stream().map( e -> BSyncStatement.make().request(e)).collect(toSet());
        
        // See what sut does
        List<BEvent> selectedBySut = new ArrayList<>(1000);
        for ( int i=0; i<1000; i++ ) {
            selectedBySut.add( sut.select(stmts, emptyList(), events).get().getEvent() );
        }
        
        // replicate with a strategy with the same seed
        EventSelectionStrategy sameSeedEss = new SimpleEventSelectionStrategy(sut.getSeed());
        List<BEvent> selectedBySameSeed = new ArrayList<>(1000);
        for ( int i=0; i<1000; i++ ) {
            selectedBySameSeed.add( sameSeedEss.select(stmts, emptyList(), events).get().getEvent() );
        }
        
        assertEquals( selectedBySut, selectedBySameSeed );
    }
}
