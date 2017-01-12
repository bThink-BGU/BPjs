/*
 *  (C) Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.eventsets;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class ExplicitEventSetTest {
    
    public ExplicitEventSetTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testOf() {
        BEvent evtOne = new BEvent("one");
        BEvent evtTwo = new BEvent("two");
        BEvent evtTre = new BEvent("tre");
        
        assertTrue( ExplicitEventSet.of(evtOne).contains(evtOne) );
        assertTrue( ExplicitEventSet.of(evtOne, evtTwo).contains(evtOne) );
        assertTrue( ExplicitEventSet.of(evtOne, evtTwo).contains(evtTwo) );
        assertFalse( ExplicitEventSet.of(evtOne, evtTwo).contains(evtTre) );
    }
    
}
