package il.ac.bgu.cs.bp.bpjs.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;

import il.ac.bgu.cs.bp.bpjs.analysis.eventpattern.EventPattern;

/**
 * @author @michbarsinai
 */
public class StatementsWithDataTest {

    @Test
    public void superStepTest() throws InterruptedException {
        SingleResourceBProgram bprog = new SingleResourceBProgram("StatementsWithData.js");
        BProgramRunner sut = new BProgramRunner();
        sut.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        sut.setBProgram(bprog);
        sut.getBProgram().setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
        sut.addListener(new BProgramRunnerListenerAdapter() {} );
        sut.run();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );

        EventPattern expected = new EventPattern()
                .append(new BEvent("1"))
                .append(new BEvent("2"))
                .append(new BEvent("3"));
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }

}