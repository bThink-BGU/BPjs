package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
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
	
    static final class Not extends ComposableEventSet {
        private final EventSet negated;

        Not(EventSet negated) {
            if ( negated == null ) throw new IllegalArgumentException("Cannot instantiate 'Not' with null event.");
            this.negated = negated;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return ! negated.contains(event);
        }

        @Override
        public EventSet negate() {
            return negated;
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
            if ( events == null ) throw new IllegalArgumentException("Cannot instantiate 'AnyOf' with null event set.");
            this.events = events;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return events.stream().anyMatch( es->es.contains(event) );
        }
        
        @Override
        public EventSet or( EventSet ifce ) {
            if ( ifce instanceof AnyOf ) {
                return new AnyOf( 
                    Stream.concat(events.stream(), ((AnyOf)ifce).events.stream())
                          .collect( toSet() ));
            } else if ( ifce instanceof EventSet ) {
                return new AnyOf( 
                    Stream.concat(events.stream(), Stream.of(ifce) )
                          .collect( toSet() ));
            } else return super.or(ifce);
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
            if ( events == null ) throw new IllegalArgumentException("Cannot instantiate 'AllOf' with null event set.");
            this.events = events;
        }
        
        @Override
        public boolean contains(BEvent event) {
            return events.stream().allMatch( es->es.contains(event) );
        }
        
        @Override
        public EventSet and( EventSet ifce ) {
            if ( ifce instanceof AllOf ) {
                return new AllOf( 
                    Stream.concat(events.stream(), ((AllOf)ifce).events.stream())
                          .collect( toSet() ));
            } else if ( ifce instanceof EventSet ) {
                return new AllOf( 
                    Stream.concat(events.stream(), Stream.of(ifce) )
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
            if ( anA == null ) throw new IllegalArgumentException("Cannot instantiate 'Xor' with null first parameter.");
            if ( aB == null ) throw new IllegalArgumentException("Cannot instantiate 'Xor' with null second parameter.");
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
    
    @Override
    public abstract boolean equals( Object o );
    
    @Override
    public abstract int hashCode();
}
