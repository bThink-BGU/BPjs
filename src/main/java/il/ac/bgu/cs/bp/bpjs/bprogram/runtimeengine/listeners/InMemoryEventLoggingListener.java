package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A BProgram listener that logs all events in an in-memory list.
 * Client code can obtain said list by calling {@link #getEvents()}.
 * 
 * @author michael
 */
public class InMemoryEventLoggingListener implements BProgramListener {
    
    private final List<BEvent> events = new ArrayList<>();
    
    
    /**
     * The list of events selected by the BProgram {@code this} instance
     * listens to. 
     * <p>
     * Returned list is not synchronized, so client code has to deal with 
     * possible concurrency issues if the list is read while the BProgram 
     * executes.
     * 
     * @return The list of events selected to far.
     */
    public List<BEvent> getEvents() {
        return events;
    }
    
    @Override
    public void starting(BProgram bp) {
    }
    
    @Override
    public void started(BProgram bp) {
    }

    @Override
    public void bthreadAdded(BProgram bp, BThreadSyncSnapshot theBThread) {
    }

    @Override
    public void bthreadRemoved(BProgram bp, BThreadSyncSnapshot theBThread) {
    }
    
    @Override
    public void bthreadDone(BProgram bp, BThreadSyncSnapshot theBThread) {
    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        events.add(theEvent);
    }

    @Override
    public void superstepDone(BProgram bp) {
    }

    @Override
    public void ended(BProgram bp) {}
    
}
