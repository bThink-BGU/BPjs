package il.ac.bgu.cs.bp.bpjs.eventsets;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.util.Collections;
import java.util.Set;

/**
 * Utility class for commonly used event sets.
 */
public class Events {
    /**
     * An event set that contains all events and event sets.
     * @author Bertrand Russel
     */
    public final static EventSet all = new EventSet() {

        @Override
        public boolean contains(Object o) {
            return (o instanceof EventSet) || (o instanceof BEvent);
        }

        @Override
        public String toString() {
            return ("{AllEvents}");
        }
    };
    
    /**
     * An event set containing no events.
     */
    public final static EventSet emptySet = new EventSet() {
        
        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public String toString() {
            return "{empty}";
        }
    };
    
    public final static Set<BEvent> noEvents = Collections.emptySet();
    
    public static <T extends BEvent> EventSet ofClass( Class<T> cls ) {
        return new EventsOfClass(cls);
    }
    
}
