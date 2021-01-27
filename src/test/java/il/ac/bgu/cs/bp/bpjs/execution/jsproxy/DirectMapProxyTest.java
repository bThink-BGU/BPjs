/*
 * The MIT License
 *
 * Copyright 2021 michael.
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
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class DirectMapProxyTest {
    
    Map<String, String> seed;
    DirectMapProxy<String, String> sut;
    
    @Before
    public void setUp() {
        seed = new HashMap<>();
        seed.put("A", "a");
        seed.put("B", "b");
        seed.put("C", "c");
        
        sut = new DirectMapProxy<>(seed);
    }
    
    
    /**
     * Test of size method, of class DirectMapProxy.
     */
    @Test
    public void testSize() {
        assertEquals( 3, sut.size() );
    }

    /**
     * Test of keys method, of class DirectMapProxy.
     */
    @Test
    public void testKeys() {
        assertEquals( Set.of("A","B","C"), sut.keys() );
    }

    /**
     * Test of has method, of class DirectMapProxy.
     */
    @Test
    public void testHas() {
        assertTrue( sut.has("A") );
        assertTrue( sut.has("B") );
        assertFalse( sut.has("X") );
    }

    /**
     * Test of get method, of class DirectMapProxy.
     */
    @Test
    public void testGet() {
        assertEquals("a", sut.get("A"));
        assertEquals("b", sut.get("B"));
        assertEquals(null, sut.get("X"));
    }

    /**
     * Test of filter method, of class DirectMapProxy.
     */
    @Test
    public void testFilter() {
        assertEquals( Map.of("A","a","B","b"), sut.filter((k,v)->!k.equals("C")));
    }
    
}
