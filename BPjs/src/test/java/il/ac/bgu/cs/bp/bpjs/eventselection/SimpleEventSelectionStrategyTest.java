package il.ac.bgu.cs.bp.bpjs.eventselection;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        Set<BSyncStatement> sets = new HashSet<>(Arrays.asList(
                BSyncStatement.make(null).request(eventOne),
                BSyncStatement.make(null).request(eventTwo),
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
}
