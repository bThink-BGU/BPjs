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
package il.ac.bgu.cs.bp.bpjs.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.ConsString;

/**
 *
 * @author michael
 */
public class BEventTest {
    
    @Test
    public void testEqualityByName() {
        assertEquals( new BEvent("a"), new BEvent("a"));
        assertEquals( new BEvent("a"), BEvent.named("a"));
        assertNotEquals(new BEvent("a"), new BEvent("b"));
    }
    
    @Test
    public void testAnnonymousEventsAreUnique()  {
        assertNotEquals( new BEvent(), new BEvent() );
        BEvent evt = new BEvent();
        assertEquals( evt, evt );
    }
    
    @Test
    public void testSimpleEqualities()  {
        final BEvent bEvent = new BEvent();
        assertEquals(bEvent, bEvent );
        assertNotEquals( bEvent, null );
        assertNotEquals( null, bEvent );
        assertNotEquals( "hello", bEvent );
    }
    
    @Test
    public void testWithData()  {
        final BEvent bEvent = new BEvent("a");
        final BEvent bEvent2 = new BEvent("a", "sampleData");
        final BEvent bEvent2Too = new BEvent("a", "sampleData");
        final BEvent bEvent3 = new BEvent("a", 3);
        assertNotEquals(bEvent, bEvent2 );
        assertNotEquals(bEvent2, bEvent );
        assertNotEquals(bEvent2, bEvent3 );
        assertEquals( bEvent2, bEvent2Too );
        assertEquals( bEvent2Too, bEvent2 );
    }
    
    @Test
    public void testWithConsString()  {
        final BEvent bEvent1 = new BEvent("a", new ConsString("a","b") );
        final BEvent bEvent2 = new BEvent("a", new ConsString("ab","") );
        final BEvent bEvent3 = new BEvent("a", new ConsString("","ab"));
        assertEquals( bEvent1, bEvent2 );
        assertEquals( bEvent2, bEvent3 );
        assertEquals( bEvent1, bEvent3 );
    }
    
    @Test
    public void testCompare()  {
        List<BEvent> events = Arrays.asList(new BEvent("b"), new BEvent("a"), new BEvent("z"));
        List<BEvent> expected = Arrays.asList(new BEvent("a"), new BEvent("b"), new BEvent("z"));
        Collections.sort(events);
        assertEquals( expected, events );
    }
    
}
