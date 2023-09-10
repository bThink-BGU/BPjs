package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import static il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet.*;
import java.util.Set;

/**
 * A <em>mathematical</em> set of events - a predicate for testing whether an
 * event is a member of a set or not. This allows for symbolic sets, such as
 * "all events" or "events of class X".
 * 
 * A subset of this class that has an internal list of events (a set in its
 * Collection Framework semantics), is {@link ExplicitEventSet}.
 * 
 * @author michael
 */
public interface EventSet extends java.io.Serializable {
	/**
	 * Implementation of the set membership function.
	 * 
	 * @param event  A candidate object to be tested for matching the criteria of  the set.
	 * @return {@code true} if the object is a member of this set.
	 */
	boolean contains(BEvent event);
    
    
    default EventSet and( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("cannot pass a null event set to and()");
		return new AllOf(Set.of(this, ifce));
	}
	
	default EventSet or( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("cannot pass a null event set to or()");
		return new AnyOf(Set.of(this, ifce) );
	}
	
	default EventSet xor( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("cannot pass a null event set to xor()");
		return new Xor(this, ifce);
	}
	
	default EventSet nor( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("cannot pass a null event set to nor()");
		return new Not( or(ifce) );
	}
	
	default EventSet nand( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("cannot pass a null event set to nand()");
		return new Not( and(ifce) );
	}
    
    default EventSet negate() {
        return new Not(this);
    }
    
    default EventSet except( final EventSet ifce ){
        if ( ifce==null ) throw new IllegalArgumentException("cannot pass a null event set to except()");
        return and(ifce.negate());
    }
    
}
