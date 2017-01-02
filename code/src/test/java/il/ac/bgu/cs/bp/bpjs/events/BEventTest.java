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
        assertNotEquals(new BEvent("a"), new BEvent("b"));
    }
    
    @Test
    public void testAnnonymousEventsAreUnique()  {
        assertNotEquals( new BEvent(), new BEvent() );
        BEvent evt = new BEvent();
        assertEquals( evt, evt );
    }
}
