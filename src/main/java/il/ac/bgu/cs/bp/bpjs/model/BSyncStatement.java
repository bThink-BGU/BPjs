package il.ac.bgu.cs.bp.bpjs.model;

import java.util.Objects;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ExplicitEventSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets.none;
import il.ac.bgu.cs.bp.bpjs.internal.OrderedSet;
import java.util.SortedSet;

/**
 * A statement a BThread makes at a {@code bsync} point. Contains data about 
 * what events it requests, waits for, and blocks, and possible additional data,
 * such as labels, or a break-upon event set.
 * 
 * @author michael
 */
public class BSyncStatement implements java.io.Serializable {

    /**
     * The event requested by this statement
     */
    private final SortedSet<? extends BEvent> request;
    
    /**
     * The events waited for (wake thread up when these happen).
     */
    private final EventSet waitFor;
    
    /**
     * The events blocked while this statement is active.
     */
    private final EventSet block;
    
    /**
     * If any of these events happen, the stating thread wants to be terminated.
     */
    private final EventSet interrupt;    
    
    /**
     * Optional data a BThread can pass to the event selector. This may serve as
     * "hot/cold", "danger level", or any other hint for a specific event selection policy.
     */
    private final Object data;
    
    private BThreadSyncSnapshot bthread;
    
    /**
     * Creates a new request where all fields are set to {@code empty}. To be
     * used as a DSL like manner:
     * <code>
     * BSyncStatement myStatement = make().request( XX ).waitFor( YY ).block( ZZZ );
     * </code>
     * @param creator the {@link BThreadSyncSnapshot} that created this statement.
     * @return an empty statement
     */
    public static BSyncStatement make(BThreadSyncSnapshot creator) {
        return new BSyncStatement(creator, Collections.emptySet(), none, none, none, null);
    }
    public static BSyncStatement make() {
        return new BSyncStatement(null, Collections.emptySet(), none, none, none, null);
    }
    
    public BSyncStatement(BThreadSyncSnapshot creator, Collection<? extends BEvent> request, EventSet waitFor, EventSet block, EventSet except, Object data) {
        this.request = new OrderedSet<>(request);
        this.waitFor = waitFor;
        this.block = block;
        this.interrupt = except;
        this.data = data;
        this.bthread = creator;
    }

    public boolean shouldWakeFor( BEvent anEvent ) {
        return request.contains(anEvent) || waitFor.contains(anEvent);
    }
    
    /**
     * Creates a new {@link BSyncStatement} based on {@code this}, with the 
     * request updated to the {@code toRequest} parameter.
     * @param toRequest the request part of the new statement
     * @return a new statement
     */
    public BSyncStatement request( Collection<? extends BEvent> toRequest ) {
        return new BSyncStatement(getBthread(), toRequest, getWaitFor(), getBlock(), getInterrupt(), getData());
    }
    public BSyncStatement request( BEvent requestedEvent ) {
        Set<BEvent> toRequest = new HashSet<>();
        toRequest.add(requestedEvent);
        return new BSyncStatement(getBthread(), toRequest, getWaitFor(), getBlock(), getInterrupt(), getData());
    }
    public BSyncStatement request( ExplicitEventSet ees ) {
        return new BSyncStatement(getBthread(), ees.getCollection(), getWaitFor(), getBlock(), getInterrupt(), getData());
    }
    
    public BSyncStatement waitFor( EventSet events ) {
        return new BSyncStatement(getBthread(), getRequest(), events, getBlock(), getInterrupt(), getData());
    }

    public BSyncStatement block( EventSet events ) {
        return new BSyncStatement(getBthread(), getRequest(), getWaitFor(), events, getInterrupt(), getData());
    }
    
    public BSyncStatement interrupt( EventSet events ) {
        return new BSyncStatement(getBthread(), getRequest(), getWaitFor(), getBlock(), events, getData());
    }
    
    public BSyncStatement data( Object someData ) { 
        return new BSyncStatement(getBthread(), getRequest(), getWaitFor(), getBlock(), getInterrupt(), someData);
    }
    
    public Collection<? extends BEvent> getRequest() {
        return request;
    }

    public EventSet getWaitFor() {
        return waitFor;
    }

    public EventSet getBlock() {
        return block;
    }

    public EventSet getInterrupt() {
        return interrupt;
    }

    public BThreadSyncSnapshot getBthread() {
        return bthread;
    }

    public BSyncStatement setBthread(BThreadSyncSnapshot bthread) {
        this.bthread = bthread;
        return this;
    }
    
    public Object getData() {
        return data;
    }
    
    public boolean hasData() {
        return data != null;
    }
    
    @Override
    public String toString() {
        return String.format("[RWBStatement r:%s w:%s b:%s i:%s d:%s]", getRequest(), getWaitFor(), getBlock(), getInterrupt(), getData());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.request);
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
        if (! (obj instanceof BSyncStatement)) {
            return false;
        }
        final BSyncStatement other = (BSyncStatement) obj;
        if (!Objects.equals(this.getRequest(), other.getRequest())) {
            return false;
        }
        if (!Objects.equals(this.getWaitFor(), other.getWaitFor())) {
            return false;
        }
        if (!Objects.equals(this.getBlock(), other.getBlock())) {
            return false;
        }
        if (!Objects.equals(this.getInterrupt(), other.getInterrupt())) {
            return false;
        }
        return Objects.equals(getData(), other.getData());
    }

}
