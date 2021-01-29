package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Utility class for commonly used event sets and event set compositions.
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
        public EventSet negate() {
            return none;
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
        public EventSet negate() {
            return all;
        }
        
        @Override
        public String toString() {
            return "{none}";
        }
        
        private Object readResolve() throws ObjectStreamException {
            return none;
        }
    };
    
    public static EventSet not( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return ifce.negate();
	}
	
    public static EventSet anyOf( final Collection<EventSet> ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new ComposableEventSet.AnyOf( new HashSet<>(ifces) );
	}
    
	public static EventSet anyOf( final EventSet... ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new ComposableEventSet.AnyOf( new HashSet<>(Arrays.asList(ifces)) );
	}
	
	public static EventSet allOf( final Collection<EventSet> ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new ComposableEventSet.AllOf(new HashSet<>(ifces));
	}
    
    public static EventSet allOf( final EventSet... ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new ComposableEventSet.AllOf(new HashSet<>(Arrays.asList(ifces)));
	}
    
}
