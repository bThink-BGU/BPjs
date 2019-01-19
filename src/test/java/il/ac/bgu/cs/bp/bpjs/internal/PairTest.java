/*
 * The MIT License
 *
 * Copyright 2019 michael.
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

import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.IntStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class PairTest {
    
    /**
     * Test of getLeft method, of class Pair.
     */
    @Test
    public void testGetLeft() {
        Pair instance = Pair.of("left", "right");
        Object expResult = "left";
        Object result = instance.getLeft();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRight method, of class Pair.
     */
    @Test
    public void testGetRight() {
        Pair instance = Pair.of("left", "right");
        Object expResult = "right";
        Object result = instance.getRight();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class Pair.
     */
    @Test
    public void testHashCodeAndEquals() {
        Set<Pair<String, String>> pairs = IntStream.rangeClosed(1, 10).mapToObj( i->Pair.of("l"+i, "r"+i)).collect( toSet() );
        assertEquals( 10, pairs.size() );
        assertTrue( pairs.contains(Pair.of("l1", "r1")));
        assertFalse( pairs.contains(Pair.of("l100", "r100")));
    }

    /**
     * Test of toString method, of class Pair.
     */
    @Test
    public void testToString() {
        Pair instance = Pair.of("left", "right");
        String result = instance.toString();
        assertTrue(result.contains("left"));
        assertTrue(result.contains("right"));
        assertTrue(result.contains("Pair"));
    }
    
    
    @Test
    public void testEquals() {
        Pair lr1 = Pair.of("left", "right");
        Pair lr2 = Pair.of("left", "right");
        Pair lx = Pair.of("left", "x");
        Pair xr = Pair.of("x", "right");
        
        assertEquals( lr1, lr1 );
        assertEquals( lr1, lr2 );
        assertNotEquals( lr1, lx );
        assertNotEquals( lr1, xr );
        assertNotEquals( lx, xr );
        assertNotEquals( lr1, null );
        assertNotEquals( lr1, "I'm a String" );
    }
}
