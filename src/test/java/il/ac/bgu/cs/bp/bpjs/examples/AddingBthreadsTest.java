package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.PrintBProgramListener;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.validation.eventpattern.EventPattern;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Tests for a bhtread that adds bthreads as it works.
 * 
 * @author @michbarsinai
 */
public class AddingBthreadsTest {
    
    @Test
    public void superStepTest() throws InterruptedException {
        
        final BEvent parentDone = new BEvent("parentDone");
        final BEvent kidADone = new BEvent("kidADone");
        final BEvent kidBDone = new BEvent("kidBDone");
        
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("AddingBthreads.js"));
        sut.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.start();
        EventSet kiddies = il.ac.bgu.cs.bp.bpjs.eventsets.ComposableEventSet.anyOf(kidADone, kidBDone);
        EventPattern expected = new EventPattern()
                .append(kiddies)
                .append(kiddies)
                .append(parentDone)
                .append(kiddies)
                .append(kiddies)
                .append(parentDone);
        
        System.out.println("Actual events:");
        eventLogger.getEvents().forEach( System.out::println );
        System.out.println("/Actual events");
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }

}