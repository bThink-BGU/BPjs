/*
 *  (C) Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class BThreadSyncSnapshotTest {
    
    /**
     * Test default naming.
     */
    @Test
    public void testDefaultNaming() {
        BThreadSyncSnapshot sut = new BThreadSyncSnapshot();
        assertEquals( BThreadSyncSnapshot.class.getName(), sut.getName() );
    }
    
}
