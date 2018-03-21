package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.analysis.eventpattern.EventPattern;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import static java.util.stream.Collectors.toList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class LoggingEventSelectionStrategyDecoratorTest {
    
    final BEvent hotEvent = new BEvent("hotEvent");
    final BEvent coldEvent = new BEvent("coldEvent");
    final BEvent allDoneEvent = new BEvent("allDone");

    @Test
    public void superStepTest() throws InterruptedException, IOException {
        StringWriter strOut = new StringWriter();
        try (PrintWriter outWriter = new PrintWriter(strOut)) {
            LoggingEventSelectionStrategyDecorator sut = new LoggingEventSelectionStrategyDecorator(new SimpleEventSelectionStrategy(), outWriter);
            
            SingleResourceBProgram bp = new SingleResourceBProgram("HotNCold.js");
            bp.setEventSelectionStrategy(sut);
            BProgramRunner runner = new BProgramRunner(bp);
            runner.addListener(new PrintBProgramRunnerListener() );
            InMemoryEventLoggingListener eventLogger = runner.addListener( new InMemoryEventLoggingListener() );
            
            runner.run();
            
            EventPattern expected = new EventPattern()
                    .append(coldEvent).append(hotEvent)
                    .append(coldEvent).append(hotEvent)
                    .append(coldEvent).append(hotEvent)
                    .append(allDoneEvent);
            
            assertTrue( expected.matches(eventLogger.getEvents()) );
            
            outWriter.flush();
        }
        strOut.flush();
        strOut.close();
        
        String output = strOut.toString();
        System.out.println("output = " + output);
        int selectableEventsLogs = Arrays.stream(output.split("\n"))
                                    .filter( s->s.contains("== Choosing Selectable Events ==") ).collect(toList()).size();
        assertEquals( 7, selectableEventsLogs );
        int eventSelections = Arrays.stream(output.split("\n"))
                                    .filter( s->s.contains("== Actual Event Selection ======") ).collect(toList()).size();
        assertEquals( 7, eventSelections );
    }
}
