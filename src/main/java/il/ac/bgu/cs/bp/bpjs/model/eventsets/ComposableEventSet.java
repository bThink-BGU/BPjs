package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;


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
	
    static final class Wrapped extends ComposableEventSet {
        private final EventSet wrappedEvent;

        Wrapped(EventSet wrappedEvent) {
            this.wrappedEvent = wrappedEvent;
        }
        
        @Override
        public ComposableEventSet and( EventSet es ) {
            return new AllOf( makeSet(this, w(es)) );
        }
        
        @Override
        public ComposableEventSet or( EventSet es ) {
            return new AnyOf( makeSet(this, w(es)) );
        }
        
        @Override
        public boolean contains(BEvent event) {
            return wrappedEvent.contains(event);
        }

        @Override
        public String toString() {
            return "(" + wrappedEvent.toString() +")";
        }
    
        @Override
        public int hashCode(){
            return 13*wrappedEvent.hashCode();
        }
        
        @Override
        public boolean equals( Object other ) {
            if ( this == other ) return true;
            if ( this == null ) return false;
            if ( other instanceof Wrapped ) {
                return ((Wrapped)other).wrappedEvent.equals(wrappedEvent);
            } else {
                return false;
            }
        }
    }
    
    static final class Not extends ComposableEventSet {
        private final EventSet negated;

        Not(EventSet negated) {
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
    
    static final class AnyOf extends ComposableEventSet {
        private final Set<EventSet> events;

        AnyOf(Set<EventSet> events) {
            this.events = events;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return events.stream().anyMatch( es->es.contains(event) );
        }
        
        @Override
        public ComposableEventSet or( EventSet ifce ) {
            if ( ifce instanceof AnyOf ) {
                return new AnyOf( 
                    Stream.concat(events.stream(), ((AnyOf)ifce).events.stream())
                          .collect( toSet() ));
            } else if ( ifce instanceof EventSet ) {
                return new AnyOf( 
                    Stream.concat(events.stream(), Stream.of(w(ifce)) )
                          .collect( toSet() ));
            } else return super.and(ifce);
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
    
    static final class AllOf extends ComposableEventSet {
        private final Set<EventSet> events;

        AllOf(Set<EventSet> events) {
            this.events = events;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return events.stream().allMatch( es->es.contains(event) );
        }
        
        @Override
        public ComposableEventSet and( EventSet ifce ) {
            if ( ifce instanceof AllOf ) {
                return new AllOf( 
                    Stream.concat(events.stream(), ((AllOf)ifce).events.stream())
                          .collect( toSet() ));
            } else if ( ifce instanceof EventSet ) {
                return new AllOf( 
                    Stream.concat(events.stream(), Stream.of(w(ifce)) )
                          .collect( toSet() ));
            } else return super.and(ifce);
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
    
    static class Xor extends ComposableEventSet {
        private final EventSet a, b;
        
        Xor( EventSet anA, EventSet aB ){
            a = anA;
            b = aB;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return a.contains(event) ^ b.contains(event);
        }

        @Override
        public String toString() {
            return "xor(" + a.toString() + ", " + b.toString() +")";
        }
    
        @Override
        public int hashCode(){
            return 23*(a.hashCode() + b.hashCode());
        }
        
        @Override
        public boolean equals( Object other ) {
            if ( this == other ) return true;
            if ( this == null ) return false;
            if ( other instanceof Xor ) {
                Xor otherXor = (Xor) other;
                return ( a.equals(otherXor.a) && b.equals(otherXor.b) )
                       || ( a.equals(otherXor.b) && b.equals(otherXor.a) );
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
			return new Wrapped(ifce);
        }
	}
	
	public static ComposableEventSet not( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return (ifce instanceof Not) ? theEventSet(((Not)ifce).negated) : new Not(w(ifce));
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
		return new AllOf(makeSet(this, w(ifce)));
	}
	
	public ComposableEventSet or( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new AnyOf( makeSet(this, w(ifce)) );
	}
	
	public ComposableEventSet xor( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new Xor(this, w(ifce));
	}
	
	public ComposableEventSet nor( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new Not( anyOf(makeSet(this, w(ifce))) );
	}
	
	public ComposableEventSet nand( final EventSet ifce ) {
        if ( ifce==null ) throw new IllegalArgumentException("eventset cannot be null");
		return new Not( allOf(makeSet(this, w(ifce))) );
	}
    
    private static ComposableEventSet w(EventSet es ) {
        return (es instanceof ComposableEventSet) ? (ComposableEventSet)es : new Wrapped(es);
    }
    
    private static Set<EventSet> makeSet( ComposableEventSet es1, EventSet es2 ) {
        Set<EventSet> retVal = new HashSet<>(2);
        retVal.add(es1);
        retVal.add(es2);
        return retVal;
    }
    
    @Override
    public abstract boolean equals( Object o );
    
    @Override
    public abstract int hashCode();
}
