package il.ac.bgu.cs.bp.bpjs.execution.listeners;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.ArrayList;
import java.util.List;
import static java.util.stream.Collectors.toList;

/**
 * A BProgram listener that logs all events in an in-memory list.
 * Client code can obtain said list by calling {@link #getEvents()}.
 * 
 * @author michael
 */
public class InMemoryEventLoggingListener extends BProgramRunnerListenerAdapter {
    
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
    
    /**
     * Convenience method for getting only the names of the events.
     * @return list of event names, by order of selection.
     */
    public List<String> eventNames() {
        return events.stream().map(BEvent::getName).collect( toList() );
    }
    
    @Override
    public void starting(BProgram bp) {
        events.clear();
    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        events.add(theEvent);
    }    
}
