package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An event set composed of actual, explicit events.
 * @author michael
 */
public class ExplicitEventSet implements EventSet {

    private final Set<BEvent> collection = new HashSet<>();
    
    public static ExplicitEventSet of( BEvent... events ) {
        ExplicitEventSet retVal = new ExplicitEventSet();
        retVal.collection.addAll(Arrays.asList(events));
        return retVal;
    }
    
    public static ExplicitEventSet from( Collection<? extends BEvent> events ) {
        ExplicitEventSet retVal = new ExplicitEventSet();
        retVal.collection.addAll(events);
        return retVal;
    }
    
    @Override
    public boolean contains(BEvent event) {
        return ( (event instanceof BEvent) &&  collection.contains((BEvent)event ) );
    }
    
    public void add( BEvent be ) {
        collection.add(be);
    }

    public Set<BEvent> getCollection() {
        return collection;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.collection);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if ( obj instanceof ExplicitEventSet ) {
            final ExplicitEventSet other = (ExplicitEventSet) obj;
            return Objects.equals(this.collection, other.collection);
        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "[EventCollection " + collection + "]";
    }
    
}
