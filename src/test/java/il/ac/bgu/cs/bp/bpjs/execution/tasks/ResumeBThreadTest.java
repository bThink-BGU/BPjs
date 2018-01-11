/*
 *  (C) Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
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
        ResumeBThread sut = new ResumeBThread(new BThreadSyncSnapshot("snap-name", null), evt, null);
        
        String toString = sut.toString();
        
        assertTrue( toString.startsWith("[ResumeBThread:") );
        assertTrue( toString.contains("snap-name") );
        assertTrue( toString.split("event:")[1].contains(evt.toString()) );
    }
    
}
