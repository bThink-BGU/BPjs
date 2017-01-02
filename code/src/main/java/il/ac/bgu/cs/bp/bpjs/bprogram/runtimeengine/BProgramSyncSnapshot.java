package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toList;

/**
 * The state of a {@link BProgram} at {@code bsync}.
 * 
 * <p>
 * For search: this class would serve as (part of?) the nodes in the search tree.
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

    public Set<BThreadSyncSnapshot> getStatements() {
        return threadSnapshots;
    }
    
    public List<BSyncStatement> syncStatements() {
        return threadSnapshots.stream()
                .map(BThreadSyncSnapshot::getBSyncStatement)
                .collect(toList());
    }
    
}
