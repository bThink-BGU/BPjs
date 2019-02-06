package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Strategy for selecting events from a set of {@link SyncStatement}s and an
 * external event queue.
 * 
 * This class has two methods, one for detecting the set of selectable events, 
 * and the other for selecting the actual event. The former is useful in both
 * execution and model checking. The latter - in execution only.
 * 
 * @author michael
 */
public interface EventSelectionStrategy {

    /**
     * Creates the set of selectable events, given a b-program's
     * synchronization point.
     * 
     * @param bpss a {@link BProgram} at a synchronization point.
     * @return A set of events that may be selected for execution.
     */
    Set<BEvent> selectableEvents(BProgramSyncSnapshot bpss);
    
    /**
     * Selects an event for execution from the parameter {@code selectableEvents},
     * or returns {@link Optional#empty()} in case no suitable event is found.
     * 
     * The {@code selectableEvents} set is Normally the set of
     * events returned by {@code this}' {@link #selectableEvents(il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot)}
     * method on the previous call on the same synchronization point. This is
     * an optimization that allows most strategies to select events only once
     * per synchronization point. 
     * 
     * <strong>In normal BP, the selected event (if any) has
     * to be a member of {@code selectableEvents}.</strong>
     * 
     * @param bpss a {@link BProgram} at a synchronization point.
     * @param selectableEvents A set of events to select from. 
     * @return An event selection result, or no result.
     */
    Optional<EventSelectionResult> select(BProgramSyncSnapshot bpss,
                                          Set<BEvent> selectableEvents );
    
}
