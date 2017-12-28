package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

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
            return ("{AllEvents}");
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
    };
    
    public final static EventSet allExcept( EventSet es ) {
        return new EventSet(){
            @Override
            public boolean contains(BEvent event) {
                return ! es.contains(event);
            }
            
            @Override
            public String toString() {
                return "{ all except " + es.toString() + "}";
            }
        };
    }
}
