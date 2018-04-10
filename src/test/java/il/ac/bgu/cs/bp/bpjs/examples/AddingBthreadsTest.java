package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.analysis.eventpattern.EventPattern;
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
        sut.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.run();
        EventSet kiddies = il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet.anyOf(kidADone, kidBDone);
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