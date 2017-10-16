package il.ac.bgu.cs.bp.bpjs.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.BProgramListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventselection.PrioritizedBSyncEventSelectionStrategy;

import il.ac.bgu.cs.bp.bpjs.validation.eventpattern.EventPattern;

/**
 * @author @michbarsinai
 */
public class StatementsWithDataTest {

    @Test
    public void superStepTest() throws InterruptedException {
        SingleResourceBProgram bprog = new SingleResourceBProgram("StatementsWithData.js");
        BProgramRunner sut = new BProgramRunner();
        sut.addListener( new StreamLoggerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        sut.setBProgram(bprog);
        sut.getBProgram().setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
        sut.addListener( new BProgramListenerAdapter() {} );
        sut.start();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );

        EventPattern expected = new EventPattern()
                .append(new BEvent("1"))
                .append(new BEvent("2"))
                .append(new BEvent("3"));
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }

}