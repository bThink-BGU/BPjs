package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class ExplicitEventSetTest {
    
    static final BEvent evtOne = new BEvent("one");
    static final BEvent evtTwo = new BEvent("two");
    static final BEvent evtTre = new BEvent("tre");
    
    @Test
    public void testOf() {
        assertTrue( ExplicitEventSet.of(evtOne).contains(evtOne) );
        assertTrue( ExplicitEventSet.of(evtOne, evtTwo).contains(evtOne) );
        assertTrue( ExplicitEventSet.of(evtOne, evtTwo).contains(evtTwo) );
        assertFalse( ExplicitEventSet.of(evtOne, evtTwo).contains(evtTre) );
    }
    
    @Test
    public void testFrom() {
        ExplicitEventSet sut = ExplicitEventSet.from(Arrays.asList(evtOne, evtTwo));
        assertTrue( sut.contains(evtOne) );
        assertTrue( sut.contains(evtTwo) );
        assertFalse( sut.contains(evtTre) );
    }
    
    @Test
    public void testEqualityAndHash() {
        ExplicitEventSet sutOf = ExplicitEventSet.of(evtOne, evtTwo);
        ExplicitEventSet sutFrom = ExplicitEventSet.from(Arrays.asList(evtOne, evtTwo));
        ExplicitEventSet sutManual = new ExplicitEventSet();
        sutManual.add(evtOne);
        sutManual.add(evtTwo);
        
        assertEquals( sutOf, sutFrom );
        assertEquals( sutOf, sutManual );
        assertEquals( sutManual, sutFrom );
        assertEquals( sutManual, sutOf );
        
        assertEquals(1, new HashSet<>(Arrays.asList(sutOf, sutManual, sutFrom)).size());
    }
    
    @Test
    public void testEqualit() {
        ExplicitEventSet sutA = ExplicitEventSet.of(evtOne, evtTwo);
        ExplicitEventSet sutB = ExplicitEventSet.of(evtOne, evtTre);
        
        assertEquals(sutA, sutA);
        assertNotEquals(sutA, sutB);
        assertNotEquals(sutA, null);
        assertNotEquals(sutA, "XYZ");
        
    }
    
    @Test
    public void testCollection() {
        ExplicitEventSet sut = ExplicitEventSet.of(evtOne, evtTwo);
        
        assertEquals( new HashSet<>(Arrays.asList(evtTwo, evtOne)), sut.getCollection());
    }
    
}
