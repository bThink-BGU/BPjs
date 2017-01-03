/*
 *  (C) Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.events;

import org.junit.Test;
import static org.junit.Assert.*;

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
}
