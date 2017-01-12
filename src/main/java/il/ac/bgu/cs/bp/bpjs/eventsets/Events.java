package il.ac.bgu.cs.bp.bpjs.eventsets;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;

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
        public boolean contains(BEvent event) {
            return (event instanceof EventSet) || (event instanceof BEvent);
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
        public boolean contains(BEvent event) {
            return false;
        }

        @Override
        public String toString() {
            return "{empty}";
        }
    };
    
}
