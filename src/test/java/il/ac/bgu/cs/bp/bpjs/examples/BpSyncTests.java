package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author orelmosheweinstock
 * @author @michbarsinai
 */
public class BpSyncTests {
    
    @Test
    public void superStepTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new SingleResourceBProgram("bp-sync.js"));
        sut.addListener( new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.run();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        
        assertEquals( "hello,world", TestUtils.eventNamesString(eventLogger.getEvents(), ",") );
    }

}