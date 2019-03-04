package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static java.util.stream.Collectors.joining;


/**
 * A wrapper class for {@link EventSet} that allows natural style logical
 * compositions of event set interfaces. Unary operators are static methods to be statically 
 * imported, and binary operators are methods of the class. <br>
 * So, assuming that A,B,C and D are {@code EventSet}s, one could write:
 * <pre><code>
 * 
 * import static bp.contrib.eventsetbooleanops.EventSetBooleanOp.*;
 * ...
 * is(A).and( not( is(B).or(C) ).xor( is(A).nand(c) ) ).contains( evt );
 * anyOf( A, B, is(C).and(not(d)) );
 * etc etc.	
 * 
 * </code></pre>
 * 
 * @author michaelbar-sinai
 */
public abstract class ComposableEventSet implements EventSet {
	
    public static final class Not extends ComposableEventSet {
        private final EventSet negated;

        public Not(EventSet negated) {
            this.negated = negated;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return ! negated.contains(event);
        }

        @Override
        public String toString() {
            return "not(" + negated.toString() +")";
        }
    
        @Override
        public int hashCode(){
            return 17*negated.hashCode();
        }
        
        @Override
        public boolean equals( Object other ) {
            if ( this == other ) return true;
            if ( this == null ) return false;
            if ( other instanceof Not ) {
                return ((Not)other).negated.equals(negated);
            } else {
                return false;
            }
        }
    }
    
    public static final class AnyOf extends ComposableEventSet {
        private final Set<EventSet> events;

        public AnyOf(Set<EventSet> events) {
            this.events = events;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return events.stream().anyMatch( es->es.contains(event) );
        }

        @Override
        public String toString() {
            return "anyOf(" + events.stream().map(es->es.toString())
                                        .sorted().collect( joining(",")) +")";
        }
    
        @Override
        public int hashCode(){
            return 17*events.hashCode();
        }
        
        @Override
        public boolean equals( Object other ) {
            if ( this == other ) return true;
            if ( this == null ) return false;
            if ( other instanceof AnyOf ) {
                return ((AnyOf)other).events.equals(events);
            } else {
                return false;
            }
        }
    }
    
    public static final class AllOf extends ComposableEventSet {
        private final Set<EventSet> events;

        public AllOf(Set<EventSet> events) {
            this.events = events;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return events.stream().allMatch( es->es.contains(event) );
        }

        @Override
        public String toString() {
            return "allOf(" + events.stream().map(es->es.toString())
                                        .sorted().collect( joining(",")) +")";
        }
    
        @Override
        public int hashCode(){
            return 19*events.hashCode();
        }
        
        @Override
        public boolean equals( Object other ) {
            if ( this == other ) return true;
            if ( this == null ) return false;
            if ( other instanceof AllOf ) {
                return ((AllOf)other).events.equals(events);
            } else {
                return false;
            }
        }
    }
    
    
	public static ComposableEventSet theEventSet( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		if ( ifce instanceof ComposableEventSet ) {
			return (ComposableEventSet) ifce;
		} else {
			return new ComposableEventSet() {
				@Override
				public boolean contains(BEvent event) {
					return ifce.contains(event);
				}

				@Override
				public String toString() {
					return "theEventSet(" + ifce.toString() +")";
				}};
		}
	}
	
	public static ComposableEventSet not( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new Not(ifce);
	}
	
    public static ComposableEventSet anyOf( final Collection<EventSet> ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new AnyOf( new HashSet<>(ifces) );
	}
    
	public static ComposableEventSet anyOf( final EventSet... ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new AnyOf( new HashSet<>(Arrays.asList(ifces)) );
	}
	
	public static ComposableEventSet allOf( final Collection<EventSet> ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new AllOf(new HashSet<>(ifces));
	}
    
    public static ComposableEventSet allOf( final EventSet... ifces) {
        if ( ifces==null ) throw new IllegalArgumentException("eventset collection cannot be null");
		return new AllOf(new HashSet<>(Arrays.asList(ifces)));
	}
	
	public ComposableEventSet and( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new AllOf(makeSet(this, ifce));
	}
	
	public ComposableEventSet or( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new AnyOf( makeSet(this, ifce) );
	}
	
	public ComposableEventSet xor( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new ComposableEventSet() {
			@Override
			public boolean contains(BEvent event) {
				return ifce.contains(event) ^ ComposableEventSet.this.contains(event);
			}
			
			@Override
			public String toString() {
				return "(" + ifce.toString() +") xor (" + ComposableEventSet.this.toString() +")";
			}};
	}
	
	public ComposableEventSet nor( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new ComposableEventSet() {
			@Override
			public boolean contains(BEvent event) {
				return !(ifce.contains(event) || ComposableEventSet.this.contains(event));
			}
			
			@Override
			public String toString() {
				return "(" + ifce.toString() +") nor (" + ComposableEventSet.this.toString() +")";
			}};
	}
	
	public ComposableEventSet nand( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new ComposableEventSet() {
			@Override
			public boolean contains(BEvent event) {
				return !(ifce.contains(event) && ComposableEventSet.this.contains(event));
			}
			
			@Override
			public String toString() {
				return "(" + ifce.toString() +") nand (" + ComposableEventSet.this.toString() +")";
			}};
	}
    
    
    private static Set<EventSet> makeSet( ComposableEventSet es1, EventSet es2 ) {
        Set<EventSet> retVal = new HashSet<>(2);
        retVal.add(es1);
        retVal.add(es2);
        return retVal;
    }
    
    @Override
    public boolean equals( Object o ) {
        if ( o == this ) return true;
        if ( o == null ) return false;
        if ( o instanceof ComposableEventSet ) {
            return toString().equals(o.toString());
        } else return false;
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
