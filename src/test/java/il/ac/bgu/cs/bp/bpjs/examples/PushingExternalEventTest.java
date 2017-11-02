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
 * @author orelmosheweinstock
 * @author @michbarsinai
 */
public class PushingExternalEventTest {
    
    @Test
    public void externalEventsFromABthread() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("PushingExternalEvent.js"));
        sut.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.start();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        EventPattern expected = new EventPattern()
                .append(new BEvent("start"))
                .append(new BEvent("external"))
                .append(new BEvent("done"));
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }
    
    @Test
    public void topLevelExternalEvents() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("TopLevelExternalEvents.js"));
        sut.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.start();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        EventPattern expected = new EventPattern()
                .append(new BEvent("ext1"))
                .append(new BEvent("ext2"))
                .append(new BEvent("ext3"))
                .append(new BEvent("internal"));
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }

}