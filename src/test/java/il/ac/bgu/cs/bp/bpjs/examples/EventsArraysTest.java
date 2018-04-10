/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Arrays;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class EventsArraysTest {

    @Test
    public void testEventsWithData() throws Exception {
        BProgramRunner bpr = new BProgramRunner(new SingleResourceBProgram("EventArrays.js"));
        bpr.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener events = bpr.addListener( new InMemoryEventLoggingListener() );
        
        bpr.run();
        
        assertEquals( Arrays.asList("e11", "e21"),
                      events.getEvents().stream().map( BEvent::getName).collect(toList()));
        
    }
    
}
