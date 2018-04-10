package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.analysis.eventpattern.EventPattern;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author @michbarsinai
 */
public class NamedArgsHotNColdTest {

    @Test
    public void superStepTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("NamedArgsHotNCold.js"));
        sut.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.run();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        final BEvent hotEvent = new BEvent("hotEvent");
        final BEvent coldEvent = new BEvent("coldEvent");
        final BEvent allDoneEvent = new BEvent("allDone");
        EventPattern expected = new EventPattern().append(coldEvent).append(hotEvent)
                .append(coldEvent).append(hotEvent)
                .append(coldEvent).append(hotEvent)
                .append(allDoneEvent);
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }

}