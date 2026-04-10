package il.ac.bgu.cs.bp.bpjs.bprogramio.log;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class BpLogTest {
    
    public BpLogTest() {
    }

    @Test
    public void testLenientValueOfOk() {
        assertEquals(BpLog.LogLevel.Info, BpLog.LogLevel.lenientValueOf(null));
        assertEquals(BpLog.LogLevel.Info, BpLog.LogLevel.lenientValueOf(""));
        assertEquals(BpLog.LogLevel.Info, BpLog.LogLevel.lenientValueOf("INFO"));
        assertEquals(BpLog.LogLevel.Info, BpLog.LogLevel.lenientValueOf("Info"));
        assertEquals(BpLog.LogLevel.Info, BpLog.LogLevel.lenientValueOf("info"));
        assertEquals(BpLog.LogLevel.Info, BpLog.LogLevel.lenientValueOf(" info "));
        assertEquals(BpLog.LogLevel.Error, BpLog.LogLevel.lenientValueOf("error"));
        assertEquals(BpLog.LogLevel.Fine, BpLog.LogLevel.lenientValueOf("FINE"));
        assertEquals(BpLog.LogLevel.Warn, BpLog.LogLevel.lenientValueOf("wArN"));
        assertEquals(BpLog.LogLevel.Off, BpLog.LogLevel.lenientValueOf("off"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testLenientValueOfBad() {
        BpLog.LogLevel.lenientValueOf("this level does not exist");
    }
}
