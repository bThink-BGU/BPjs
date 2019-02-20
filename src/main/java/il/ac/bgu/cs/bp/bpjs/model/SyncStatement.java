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
 * A statement a BThread makes at a synchronization point (i.e when {@code bp.sync} is called).
 * Contains data about what events it requests, waits for, and blocks, and possible additional data,
 * such as labels, or a break-upon event set.
 * 
 * @author michael
 */
public class SyncStatement implements java.io.Serializable {

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
     * When {@code true}, the b-thread states that it cannot stay in this
     * synchronization point forever.
     */
    private final boolean hot;
    
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
    public static SyncStatement make(BThreadSyncSnapshot creator) {
        return new SyncStatement(creator, Collections.emptySet(), none, none, none, false, null);
    }
    public static SyncStatement make() {
        return new SyncStatement(null, Collections.emptySet(), none, none, none, false, null);
    }
    
    public SyncStatement(BThreadSyncSnapshot creator, Collection<? extends BEvent> request, EventSet waitFor, EventSet block, EventSet except, boolean isHot, Object data) {
        this.request = new OrderedSet<>(request);
        this.waitFor = waitFor;
        this.block = block;
        this.interrupt = except;
        this.hot = isHot;
        this.data = data;
        this.bthread = creator;
    }

    public boolean shouldWakeFor( BEvent anEvent ) {
        return request.contains(anEvent) || waitFor.contains(anEvent);
    }
    
    /**
     * Creates a new {@link SyncStatement} based on {@code this}, with the 
     * request updated to the {@code toRequest} parameter.
     * @param toRequest the request part of the new statement
     * @return a new statement
     */
    public SyncStatement request( Collection<? extends BEvent> toRequest ) {
        return new SyncStatement(getBthread(), toRequest, getWaitFor(), getBlock(), getInterrupt(), isHot(), getData());
    }
    public SyncStatement request( BEvent requestedEvent ) {
        Set<BEvent> toRequest = new HashSet<>();
        toRequest.add(requestedEvent);
        return new SyncStatement(getBthread(), toRequest, getWaitFor(), getBlock(), getInterrupt(), isHot(), getData());
    }
    public SyncStatement request( ExplicitEventSet ees ) {
        return new SyncStatement(getBthread(), ees.getCollection(), getWaitFor(), getBlock(), getInterrupt(), isHot(), getData());
    }
    
    public SyncStatement waitFor( EventSet events ) {
        return new SyncStatement(getBthread(), getRequest(), events, getBlock(), getInterrupt(), isHot(), getData());
    }

    public SyncStatement block( EventSet events ) {
        return new SyncStatement(getBthread(), getRequest(), getWaitFor(), events, getInterrupt(), isHot(), getData());
    }
    
    public SyncStatement interrupt( EventSet events ) {
        return new SyncStatement(getBthread(), getRequest(), getWaitFor(), getBlock(), events, isHot(), getData());
    }
    
    public SyncStatement hot( boolean shouldBeHot ) { 
        return new SyncStatement(getBthread(), getRequest(), getWaitFor(), getBlock(), getInterrupt(), shouldBeHot, getData());
    }
    
    public SyncStatement data( Object someData ) { 
        return new SyncStatement(getBthread(), getRequest(), getWaitFor(), getBlock(), getInterrupt(), isHot(), someData);
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

    public SyncStatement setBthread(BThreadSyncSnapshot bthread) {
        this.bthread = bthread;
        return this;
    }
    
    public Object getData() {
        return data;
    }
    
    public boolean hasData() {
        return data != null;
    }

    public boolean isHot() {
        return hot;
    }
        
    @Override
    public String toString() {
        return String.format("[SyncStatement%s r:%s w:%s b:%s i:%s d:%s]", isHot()?"<hot>":"",
                getRequest(), getWaitFor(), getBlock(), getInterrupt(), getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequest(), getWaitFor(), getBlock(),
                            getInterrupt(), getData(), isHot());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        
        if (! (obj instanceof SyncStatement)) {
            return false;
        }
        
        final SyncStatement other = (SyncStatement) obj;
        if ( this.isHot() != other.isHot() ) {
            return false;
        }
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
