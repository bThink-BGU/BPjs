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
package il.ac.bgu.cs.bp.bpjs.internal;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class OrderedSetTest {
    

    /**
     * Test of of method, of class OrderedSet.
     */
    @Test
    public void testBasics() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c", "c");
        assertEquals("a", sut.first());
        assertEquals("c", sut.last());
        assertEquals("b", sut.tailSet("b").first());
        assertEquals("c", sut.tailSet("b").last());
        
        assertEquals( new HashSet<>(Arrays.asList("a","b","c")), sut);
    }
    
    @Test
    public void testEquals() {
        OrderedSet<String> sutA = OrderedSet.of();
        OrderedSet<String> sutB = OrderedSet.of();
        
        assertEquals(sutA, sutA);
        assertNotEquals(sutA, null);
        assertNotEquals("not a set", sutA);
        
        assertEquals(sutB, sutA);
        
        sutA.add("A");
        assertNotEquals(sutA, sutB);
        assertNotEquals(sutB, sutA);
        
        sutB.add("A");
        assertEquals(sutA, sutB);
        assertEquals(sutB, sutA);
        
        // double addition should be ignored
        sutA.add("A");
        assertEquals(sutA, sutB);
        assertEquals(sutB, sutA);
        
        sutA.add("X");
        assertNotEquals(sutA, sutB);
        assertNotEquals(sutB, sutA);
    }
    
    @Test
    public void testHeadTailFirstLastSets() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c","d");
        assertEquals( OrderedSet.of("b","c","d"), sut.tailSet("b"));
        assertTrue( sut.tailSet("d").size() == 1 );
        
        assertEquals( OrderedSet.of("a","b"), sut.headSet("c") );
        assertTrue( sut.headSet("NOT-THERE").isEmpty() );
        
        assertEquals("a", sut.first());
        assertEquals("d", sut.last());
        
    }
    
    @Test
    public void testSubSet() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c","d","e","f");
        assertEquals( OrderedSet.of("b","c","d"), sut.subSet("b", "e"));
    }
    
    @Test
    public void testToArrays() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c","d");
        assertArrayEquals( new Object[]{"a","b","c","d"}, sut.toArray() );
        assertArrayEquals( new String[]{"a","b","c","d"}, sut.toArray(new String[4]) );
    }
    
    @Test
    public void testRemove() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c","d");
        assertTrue( sut.remove("c") );
        assertEquals( OrderedSet.of("a","b","d"), sut );
        assertFalse( sut.remove("c")  );
        assertEquals( OrderedSet.of("a","b","d"), sut );
        assertFalse( sut.remove("NOT_THERE") );
        assertEquals( OrderedSet.of("a","b","d"), sut );
    }
    
    @Test
    public void testContainsAll() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c","d");
        assertTrue( sut.containsAll(Arrays.asList("a","b")) );
        assertFalse( sut.containsAll(Arrays.asList("a","NOT_THERE")) );
    }
    
    @Test
    public void testClear() {
        OrderedSet<String> sut = OrderedSet.of("a","b","c","d");
        assertEquals( 4, sut.size() );
        assertFalse( sut.isEmpty() );
        sut.clear();
        assertTrue( sut.isEmpty() );
        assertEquals( 0, sut.size() );
    }
    
    
    
}
