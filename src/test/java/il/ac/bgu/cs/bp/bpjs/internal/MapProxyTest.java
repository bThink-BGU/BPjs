/*
 * The MIT License
 *
 * Copyright 2020 michael.
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class MapProxyTest {
    
    private Map<String, Integer> seedMap;
    
    @Before
    public void setUp() {
        seedMap = Map.of("a", 1,
            "b",2,
            "c",3,
            "d",4
        );
    }
    
    /**
     * Test of put method, of class MapProxy.
     */
    @Test
    public void testOverwrite() {
        MapProxy<String, Integer> sut = new MapProxy<>(seedMap);
        assertEquals( 4, sut.get("d").intValue() );
        
        sut.put( "d", 500);
        assertEquals( 500, sut.get("d").intValue() );
        assertTrue( sut.has("d") );
        
        assertEquals( seedMap.size(), sut.size() );
        assertEquals( seedMap.keySet(), sut.keys() );
        
        sut.put( "d", 5001);
        assertEquals( 5001, sut.get("d").intValue() );
    }

    /**
     * Test of remove method, of class MapProxy.
     */
    @Test
    public void testRemove() {
        MapProxy<String, Integer> sut = new MapProxy<>(seedMap);
        assertEquals( 4, sut.get("d").intValue() );
        assertTrue( sut.has("d") );
        
        sut.remove("d");
        assertEquals( null, sut.get("d") );
        assertFalse( sut.has("d") );
        
        assertEquals( seedMap.size()-1, sut.size() );
        Set<String> expectedKeys = new HashSet<>(seedMap.keySet());
        
        expectedKeys.remove("d");
        assertEquals(expectedKeys, sut.keys() );
        
    }
    
    @Test
    public void testRemoveAdded() {
        MapProxy<String, Integer> sut = new MapProxy<>(seedMap);
        
        sut.put( "z", 500);
        assertEquals( 500, sut.get("z").intValue() );
        assertTrue( sut.has("z") );
        
        sut.remove("z");
        assertEquals( null, sut.get("z") );
        assertFalse( sut.has("z") );
        assertEquals( 4, sut.size() );
    }

    @Test
    public void testAdd() {
        MapProxy<String, Integer> sut = new MapProxy<>(seedMap);
        
        sut.put( "x", 500);
        sut.put( "y", 501);
        sut.put( "z", 502);
        assertEquals( 500, sut.get("x").intValue() );
        assertEquals( 501, sut.get("y").intValue() );
        assertEquals( 502, sut.get("z").intValue() );
        assertTrue( sut.has("x") );
        assertTrue( sut.has("y") );
        assertTrue( sut.has("z") );
        
        Set<String> expectedKeys = new HashSet<>(seedMap.keySet());
        expectedKeys.add("x");
        expectedKeys.add("y");
        expectedKeys.add("z");
        assertEquals(expectedKeys, sut.keys() );
    }
   

    /**
     * Test of reset method, of class MapProxy.
     */
    @Test
    public void testReset() {
        MapProxy<String, Integer> sut = new MapProxy<>(seedMap);
        assertFalse( sut.has("z") );
        assertTrue( sut.has("d") );
        
        sut.put("z", 500);
        sut.remove("d");
        
        assertTrue( sut.has("z") );
        assertFalse( sut.has("d") );
        
        sut.reset();
        
        assertFalse( sut.has("z") );
        assertTrue( sut.has("d") );
    }
    
}
