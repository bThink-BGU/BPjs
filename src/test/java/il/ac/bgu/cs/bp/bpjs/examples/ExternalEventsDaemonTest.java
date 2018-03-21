package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.analysis.eventpattern.EventPattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author michael
 */
public class ExternalEventsDaemonTest {
    
    @Test
    public void superStepTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner( new SingleResourceBProgram("ExternalEventsDaemon.js"));
        sut.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        new Thread( ()-> {
            try {
                for ( int i=0; i<4; i++ ) {
                    Thread.sleep(500);
                    sut.getBProgram().enqueueExternalEvent(new BEvent("ext1"));
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ExternalEventsDaemonTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } ).start();
        
        sut.run();
        
        assertTrue( sut.getBProgram().getFromGlobalScope("internalDaemonMode", Boolean.class).get() );
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        
        final BEvent in1a = new BEvent("in1a");
        final BEvent in1b = new BEvent("in1b");
        final BEvent ext1 = new BEvent("ext1");

        EventPattern expected = new EventPattern()
                .append(ext1).append(in1a).append(in1b)
                .append(ext1).append(in1a).append(in1b)
                .append(ext1).append(in1a).append(in1b);
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
        
                
    }    
}
