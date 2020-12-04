package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.*;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class ResumeBThreadTest {
    
    /**
     * Test of toString method, of class ResumeBThread.
     */
    @Test
    public void testToString() {
        BEvent evt = new BEvent("evtName");
        BThreadSyncSnapshot bt = new BThreadSyncSnapshot("snap-name", null, new StringBProgram(""));
        ResumeBThread sut = new ResumeBThread(makeBPSS(bt), bt, evt, null);
        
        String toString = sut.toString();
        
        assertTrue( toString.startsWith("[ResumeBThread:") );
        assertTrue( toString.contains("snap-name") );
        assertTrue( toString.split("event:")[1].contains(evt.toString()) );
    }
    
}
