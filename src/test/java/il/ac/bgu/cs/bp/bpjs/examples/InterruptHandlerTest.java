package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.exceptions.BProgramException;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.PrintBProgramListener;
import il.ac.bgu.cs.bp.bpjs.verification.eventpattern.EventPattern;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author @michbarsinai
 */
public class InterruptHandlerTest {

    @Test
    public void echoEventTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("InterruptHandler.js") );
        sut.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.start();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        EventPattern expected = new EventPattern()
                .append(new BEvent("boom"))
                .append(new BEvent("boom"))
                .append(new BEvent("internalValue"));
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }
    
    @Test(expected=BProgramException.class)
    public void illegalBsyncTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("InterruptHandler_illegal.js") );
        sut.addListener(new PrintBProgramListener() );
        
        sut.start();
        
        fail("Program should have terminated in error.");
    }

}