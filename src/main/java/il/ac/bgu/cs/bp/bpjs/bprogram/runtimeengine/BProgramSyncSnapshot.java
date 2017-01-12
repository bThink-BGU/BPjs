package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.util.List;
import java.util.Set;

/**
 * The state of a {@link BProgram} when all its BThreads are at {@code bsync}.
 * This is more than a set of {@link BThreadSyncSnapshot}s, as it contains
 * the queue of external events as well.
 * 
 * <p>
 * For search: this class could serve as (part of?) the nodes in the search tree.
 * </p>
 * 
 * @author michael
 */
public class BProgramSyncSnapshot {
    
    private final Set<BThreadSyncSnapshot> threadSnapshots;
    private final List<BEvent> externalEvents;

    public BProgramSyncSnapshot(Set<BThreadSyncSnapshot> threadSnapshots, List<BEvent> externalEvents) {
        this.threadSnapshots = threadSnapshots;
        this.externalEvents = externalEvents;
    }

    public List<BEvent> getExternalEvents() {
        return externalEvents;
    }

    public Set<BThreadSyncSnapshot> getBThreadSnapshots() {
        return threadSnapshots;
    }
    
}
