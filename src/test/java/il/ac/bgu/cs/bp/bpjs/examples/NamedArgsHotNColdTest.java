package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.PrintBProgramListener;
import il.ac.bgu.cs.bp.bpjs.validation.eventpattern.EventPattern;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author @michbarsinai
 */
public class NamedArgsHotNColdTest {

    @Test
    public void superStepTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("NamedArgsHotNCold.js"));
        sut.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.start();
        
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