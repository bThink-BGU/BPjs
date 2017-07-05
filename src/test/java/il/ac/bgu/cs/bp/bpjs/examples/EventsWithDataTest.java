/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventselection.SimpleEventSelectionStrategy;
import java.util.Arrays;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class EventsWithDataTest {

    @Test
    public void testEventsWithData() throws Exception {
        BProgram bpr = new SingleResourceBProgram( "EventsWithData.js", "programName", new SimpleEventSelectionStrategy(99l) );
        BProgramRunner rnr = new BProgramRunner(bpr);
        rnr.addListener( new StreamLoggerListener() );
        InMemoryEventLoggingListener events = rnr.addListener( new InMemoryEventLoggingListener() );
        
        rnr.start();
        
        assertEquals( Arrays.asList("e1", "e2", "e1e2"),
                      events.getEvents().stream().map( BEvent::getName).collect(toList()) );
        
    }
    
}
