package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.io.ObjectStreamException;

/**
 * Utility class for commonly used event sets.
 */
public final class EventSets {
    
    private EventSets(){
        // prevent instantiation.
    }
    
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
            return ("{all}");
        }
        
        private Object readResolve() throws ObjectStreamException {
            return all;
        }
    };
    
    /**
     * An event set containing no events.
     */
    public final static EventSet none = new EventSet() {
        
        @Override
        public boolean contains(BEvent event) {
            return false;
        }

        @Override
        public String toString() {
            return "{none}";
        }
        
        private Object readResolve() throws ObjectStreamException {
            return none;
        }
    };
    
}
