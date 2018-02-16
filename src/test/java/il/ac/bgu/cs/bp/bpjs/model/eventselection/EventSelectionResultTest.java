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
package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

/**
 *
 * @author michael
 */
public class EventSelectionResultTest {
    
    public EventSelectionResultTest() {
    }

    /**
     * Test of toString method, of class EventSelectionResult.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNoNegIndicies() {
        EventSelectionResult ess = new EventSelectionResult(new BEvent("n"), new HashSet<>(Arrays.asList(-1,-2,-3)));
    }
    
    @Test
    public void testEquality() {
        EventSelectionResult esrA = new EventSelectionResult( new BEvent("a") );
        EventSelectionResult esrA1 = new EventSelectionResult( new BEvent("a"), new HashSet<>(Arrays.asList(1) ));
        EventSelectionResult esrA2 = new EventSelectionResult( new BEvent("a"), new HashSet<>(Arrays.asList(2) ));
        EventSelectionResult esrB = new EventSelectionResult( new BEvent("b") );
        
        assertEquals(esrA, esrA);
        assertNotEquals(esrA, esrB);
        assertNotEquals(esrA, esrA1);
        assertNotEquals(esrA2, esrA1);
        assertNotEquals(esrA, null);
        assertNotEquals(esrA, "hello");
        
        Set<EventSelectionResult> ess = new HashSet<>();
        ess.add(esrA);
        ess.add(esrA);
        ess.add(esrA1);
        ess.add(esrA1);
        ess.add(esrA2);
        ess.add(esrA2);
        assertEquals( 3, ess.size() );
    }
    
}
