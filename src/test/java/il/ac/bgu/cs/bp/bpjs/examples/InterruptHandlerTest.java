package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.analysis.eventpattern.EventPattern;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.junit.Assert.fail;

/**
 * @author @michbarsinai
 */
public class InterruptHandlerTest {

    @Test
    public void echoEventTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new ResourceBProgram("InterruptHandler.js") );
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
    
    @Test
    public void illegalBsyncTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new ResourceBProgram("InterruptHandler_illegal.js") );
        sut.addListener(new PrintBProgramRunnerListener() );
        
        final AtomicBoolean errorCalled = new AtomicBoolean();
        final AtomicBoolean errorMadeSense = new AtomicBoolean();
        
        sut.addListener( new BProgramRunnerListenerAdapter() {
            @Override
            public void error(BProgram bp, Exception ex) {
                errorCalled.set(true);
                System.out.println(ex.getMessage());
                errorMadeSense.set(ex.getMessage().contains("Cannot call bp.sync"));
            }
        });

        sut.run();
        
        assertTrue( errorCalled.get() );
        assertTrue( errorMadeSense.get() );
    }

}