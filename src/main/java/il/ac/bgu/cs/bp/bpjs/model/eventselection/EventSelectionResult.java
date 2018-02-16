package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Collections;
import java.util.Set;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * The result of selecting an event. Contains the {@link #selectedEvent} itself,
 * and a set of indices to remove from the external event queue.
 *
 * This set allows {@link EventSelectionStrategy}s to make the external event
 * queue act like, e.g., a set of events rather than a list.
 *
 * @author michael
 */
public class EventSelectionResult {

    private final BEvent selectedEvent;
    private final Set<Integer> indicesToRemove;

    public EventSelectionResult(BEvent aSelectedEvent, Set<Integer> someIndices) {
        selectedEvent = aSelectedEvent;
        indicesToRemove = someIndices;
        Set<Integer> negIndices = indicesToRemove.stream().filter(i -> i < 0).collect(toSet());
        if (!negIndices.isEmpty()) {
            throw new IllegalArgumentException("The following indices are illegal: "
                    + negIndices.stream().map(Object::toString).collect(joining(",")));
        }
    }

    public EventSelectionResult(BEvent anEvent) {
        this(anEvent, Collections.emptySet());
    }

    @Override
    public String toString() {
        return "[EventSelectionResult event:" + getEvent() + " indices:" + getIndicesToRemove() + "]";
    }

    public BEvent getEvent() {
        return selectedEvent;
    }

    /**
     * Set of indices of events in the external event queue. These events will
     * be removed from the queue by the {@link BProgram} when the selected event
     * is triggered. 
     * @return Set of event indices in the external events queue.
     */
    public Set<Integer> getIndicesToRemove() {
        return indicesToRemove;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.indicesToRemove.hashCode();
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EventSelectionResult other = (EventSelectionResult) obj;
        return selectedEvent.equals(other.getEvent())
                && indicesToRemove.equals(other.getIndicesToRemove());
    }

}

