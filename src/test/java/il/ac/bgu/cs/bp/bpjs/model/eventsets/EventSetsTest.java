/*
 * The MIT License
 *
 * Copyright 2017 michael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class EventSetsTest {
    
    private static final BEvent EVT_1 = new BEvent("EVT_1");
    private static final BEvent EVT_2 = new BEvent("EVT_2");
    private static final BEvent EVT_3 = new BEvent("EVT_3");
    private static final BEvent EVT_4 = new BEvent("EVT_4");
    private static final BEvent EVT_4_SAME = new BEvent("EVT_4");

    /**
     * Test of allExcept method, of class EventSets.
     */
    @Test
    public void testAllExcept() {
        EventSet es = EventSets.allExcept(EVT_4);
        
        assertFalse( es.contains(EVT_4) );
        assertFalse( es.contains(EVT_4_SAME) );
        assertTrue( es.contains(EVT_1) );
        assertTrue( es.contains(EVT_2) );
        assertTrue( es.contains(EVT_3) );
        
        assertTrue( es.toString().contains(EVT_4.getName()) );
    }
    
    @Test
    public void testAll() {
        assertTrue( EventSets.all.contains(EVT_1) );
        assertTrue( EventSets.all.contains(EVT_2) );
        assertTrue( EventSets.all.contains(EVT_3) );
        assertTrue( EventSets.all.contains(EVT_4) );
        assertTrue( EventSets.all.contains(EVT_4_SAME) );
        assertTrue( EventSets.all.toString().contains("All") );
    }
    
    @Test
    public void testNone() {
        // OK, quite anecdotal, but still.
        assertFalse( EventSets.none.contains(EVT_1) );
        assertFalse( EventSets.none.contains(EVT_2) );
        assertFalse( EventSets.none.contains(EVT_3) );
        assertFalse( EventSets.none.contains(EVT_4) );
        assertFalse( EventSets.none.contains(EVT_4_SAME) );
        assertTrue( EventSets.none.toString().contains("none") );
    }
    
}
