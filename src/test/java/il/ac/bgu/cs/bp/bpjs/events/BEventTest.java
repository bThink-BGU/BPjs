/*
 *  (C) Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.events;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.ConsString;

/**
 *
 * @author michael
 */
public class BEventTest {
    
    @Test
    public void testEqualityByName() {
        assertEquals( new BEvent("a"), new BEvent("a"));
        assertEquals( new BEvent("a"), BEvent.named("a"));
        assertNotEquals(new BEvent("a"), new BEvent("b"));
    }
    
    @Test
    public void testAnnonymousEventsAreUnique()  {
        assertNotEquals( new BEvent(), new BEvent() );
        BEvent evt = new BEvent();
        assertEquals( evt, evt );
    }
    
    @Test
    public void testSimpleEqualities()  {
        final BEvent bEvent = new BEvent();
        assertEquals(bEvent, bEvent );
        assertFalse( bEvent.equals(null) );
    }
    
    @Test
    public void testWithData()  {
        final BEvent bEvent = new BEvent("a");
        final BEvent bEvent2 = new BEvent("a", "sampleData");
        final BEvent bEvent2Too = new BEvent("a", "sampleData");
        assertNotEquals(bEvent, bEvent2 );
        assertNotEquals(bEvent2, bEvent );
        assertEquals( bEvent2, bEvent2Too );
        assertEquals( bEvent2Too, bEvent2 );
    }
    
    @Test
    public void testWithConsString()  {
        final BEvent bEvent1 = new BEvent("a", new ConsString("a","b") );
        final BEvent bEvent2 = new BEvent("a", new ConsString("ab","") );
        final BEvent bEvent3 = new BEvent("a", new ConsString("","ab"));
        assertEquals( bEvent1, bEvent2 );
        assertEquals( bEvent2, bEvent3 );
        assertEquals( bEvent1, bEvent3 );
    }
    
    @Test
    public void testCompare()  {
        List<BEvent> events = Arrays.asList(new BEvent("b"), new BEvent("a"), new BEvent("z"));
        List<BEvent> expected = Arrays.asList(new BEvent("a"), new BEvent("b"), new BEvent("z"));
        Collections.sort(events);
        assertEquals( expected, events );
    }
    
}
