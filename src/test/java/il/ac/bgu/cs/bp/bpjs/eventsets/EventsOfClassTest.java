/*
 *  (C) Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.eventsets;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class EventsOfClassTest {
    
    static class XEvent extends BEvent {
        XEvent( String name ) {
            super ("X" + name );
        }
    }
    static class YEvent extends BEvent {
        YEvent( String name ) {
            super ("Y" + name );
        }
    }
    
    @Test
    public void testContains() {
        BEvent be = new BEvent("b1");
        BEvent xe = new XEvent("x1");
        BEvent ye = new YEvent("y1");
        
        assertTrue( new EventsOfClass(XEvent.class).contains(xe) );
        assertTrue( new EventsOfClass(BEvent.class).contains(xe) );
        
        assertFalse( new EventsOfClass(XEvent.class).contains(ye) );
        assertFalse( new EventsOfClass(XEvent.class).contains(be) );
        
        assertFalse( new EventsOfClass(XEvent.class, YEvent.class).contains(be) );
    }
    
}
