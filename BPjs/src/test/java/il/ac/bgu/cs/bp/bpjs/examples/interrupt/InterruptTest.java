package il.ac.bgu.cs.bp.bpjs.examples.interrupt;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mozilla.javascript.Scriptable;

/**
 * @author orelmosheweinstock
 * @author @michbarsinai
 */
public class InterruptTest {

    BProgram buildProgram() {
        return new BProgram("Interrupt") {
            @Override
            protected void setupProgramScope( Scriptable aScope ) {
                loadJavascriptResource("Interrupt.js");
            }
        };
    }
    
    @Test
    public void superStepTest() throws InterruptedException {
        BProgram sut = buildProgram();
        sut.addListener( new StreamLoggerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.start();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        final BEvent breakingEvent = new BEvent("breaking");
        
        assertEquals( Arrays.asList(breakingEvent, breakingEvent), eventLogger.getEvents() );
    }

}