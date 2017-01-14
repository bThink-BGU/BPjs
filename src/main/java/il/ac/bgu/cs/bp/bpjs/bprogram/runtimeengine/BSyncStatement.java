package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.util.Objects;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.ExplicitEventSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static il.ac.bgu.cs.bp.bpjs.eventsets.Events.emptySet;

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
    private final Set<BEvent> request;
    
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
        return new BSyncStatement(Collections.emptySet(), emptySet, emptySet, emptySet).setBthread(creator);
    }
    public static BSyncStatement make() {
        return new BSyncStatement(Collections.emptySet(), emptySet, emptySet, emptySet);
    }
    
    public BSyncStatement(Collection<? extends BEvent> request, EventSet waitFor, EventSet block, EventSet except) {
        this.request = new HashSet<>(request);
        this.waitFor = waitFor;
        this.block = block;
        this.interrupt = except;
    }

    public BSyncStatement(Collection<? extends BEvent> request, EventSet waitFor, EventSet block) {
        this(request, waitFor, block, emptySet);
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
        return new BSyncStatement(toRequest, getWaitFor(), getBlock(), getInterrupt());
    }
    public BSyncStatement request( BEvent requestedEvent ) {
        Set<BEvent> toRequest = new HashSet<>();
        toRequest.add(requestedEvent);
        return new BSyncStatement(toRequest, getWaitFor(), getBlock(), getInterrupt());
    }
    public BSyncStatement request( ExplicitEventSet ees ) {
        return new BSyncStatement(ees.getCollection(), getWaitFor(), getBlock(), getInterrupt());
    }
    
    public BSyncStatement waitFor( EventSet events ) {
        return new BSyncStatement(getRequest(), events, getBlock(), getInterrupt());
    }

    public BSyncStatement block( EventSet events ) {
        return new BSyncStatement(getRequest(), getWaitFor(), events, getInterrupt());
    }
    
    public BSyncStatement interrupt( EventSet events ) {
        return new BSyncStatement(getRequest(), getWaitFor(), getBlock(), events);
    }
    
    public Collection<BEvent> getRequest() {
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
    
    @Override
    public String toString() {
        return String.format("[RWBStatement r:%s w:%s b:%s i:%s]", getRequest(), getWaitFor(), getBlock(), getInterrupt());
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
        return Objects.equals(this.getInterrupt(), other.getInterrupt());
    }

}
