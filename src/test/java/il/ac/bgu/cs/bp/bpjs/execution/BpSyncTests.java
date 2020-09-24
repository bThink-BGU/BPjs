package il.ac.bgu.cs.bp.bpjs.execution;

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * @author orelmosheweinstock
 * @author @michbarsinai
 */
public class BpSyncTests {
    
    @Test
    public void superStepTest() throws InterruptedException {
        BProgramRunner sut = new BProgramRunner(new ResourceBProgram("bp-sync.js"));
        sut.addListener( new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        
        sut.run();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );
        
        assertEquals( "hello,world", TestUtils.eventNamesString(eventLogger.getEvents(), ",") );
    }
    
    
    @Test
    public void eventSetSpecialCasesTest() throws InterruptedException, Exception {
        
        BProgram bprog = new ResourceBProgram("EventSetSpecialCases.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        VerificationResult res = vfr.verify(bprog);
        
        var exTrace = res.getViolation().get().getCounterExampleTrace();
        var snapshots = exTrace.getNodes().get(0).getState().getBThreadSnapshots()
                            .stream().collect( Collectors.toMap(s->s.getName(), Function.identity()));
        
        assertEquals( EventSets.none, snapshots.get("n1").getSyncStatement().getWaitFor() );
        assertEquals( EventSets.none, snapshots.get("n2").getSyncStatement().getWaitFor() );
        assertEquals( EventSets.none, snapshots.get("n3").getSyncStatement().getWaitFor() );
        
        assertEquals( 
            snapshots.get("es12").getSyncStatement().getWaitFor(),
            snapshots.get("es21").getSyncStatement().getWaitFor()
        );
        
    }
}