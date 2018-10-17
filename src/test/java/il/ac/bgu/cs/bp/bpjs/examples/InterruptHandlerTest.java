package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.analysis.eventpattern.EventPattern;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
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
        sut.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.run();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        EventPattern expected = new EventPattern()
                .append(new BEvent("boom"))
                .append(new BEvent("boom"))
                .append(new BEvent("internalValue"));
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }
    
    @Test(expected=BPjsRuntimeException.class)
    public void illegalBsyncTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("InterruptHandler_illegal.js") );
        sut.addListener(new PrintBProgramRunnerListener() );
        
        sut.run();
        
        fail("Program should have terminated in error.");
    }

}