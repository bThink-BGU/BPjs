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

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.internal.MapProxyConsolidator.Conflict;
import il.ac.bgu.cs.bp.bpjs.internal.MapProxyConsolidator.Success;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class MapProxyConsolidatorTest {
    
    Map<String, Object> baseMap;
    
    @Before
    public void setUp() {
        baseMap = Map.of("a","A","b","B","c","C");
    }
    

    /**
     * Test of consolidate method, of class MapProxyConsolidator.
     */
    @Test
    public void testNoConflict_noCrossModifications() {
        MapProxy mp1 = new MapProxy(baseMap);
        mp1.put("x", "X");
        
        MapProxy mp2 = new MapProxy(baseMap);
        mp2.put("y", "Y");
        
        MapProxy mp3 = new MapProxy(baseMap);
        mp3.remove("a");
        
        MockBThreadSyncSnapshot mbt1 = TestUtils.makeMockBtss();
        mbt1.setBprogramStoreModifications(mp1);
        MockBThreadSyncSnapshot mbt2 = TestUtils.makeMockBtss();
        mbt2.setBprogramStoreModifications(mp2);
        MockBThreadSyncSnapshot mbt3 = TestUtils.makeMockBtss();
        mbt3.setBprogramStoreModifications(mp3);
        
        var sut = new MapProxyConsolidator();
        Set<BThreadSyncSnapshot> snaps = Set.<BThreadSyncSnapshot>of(mbt1, mbt2, mbt3);
        var actual = sut.consolidate( snaps);
        
        assertTrue( actual instanceof Success );
        assertEquals(
            Map.of("b","B", "c","C", "x","X", "y","Y"),
            ((Success)actual).apply(baseMap)
        );
    }
    
    @Test
    public void testNoConflict_AgreeingModifications() {
        // need to test both deletes and puts
        MapProxy mp1 = new MapProxy(baseMap);
        mp1.put("x", "X");
        
        MapProxy mp2 = new MapProxy(baseMap);
        mp2.put("x", "X");
        
        MapProxy mp3 = new MapProxy(baseMap);
        mp3.remove("a");
        
        MapProxy mp4 = new MapProxy(baseMap);
        mp4.remove("a");
        
        MockBThreadSyncSnapshot mbt1 = TestUtils.makeMockBtss();
        mbt1.setBprogramStoreModifications(mp1);
        MockBThreadSyncSnapshot mbt2 = TestUtils.makeMockBtss();
        mbt2.setBprogramStoreModifications(mp2);
        MockBThreadSyncSnapshot mbt3 = TestUtils.makeMockBtss();
        mbt3.setBprogramStoreModifications(mp3);
        MockBThreadSyncSnapshot mbt4 = TestUtils.makeMockBtss();
        mbt4.setBprogramStoreModifications(mp4);
        
        var sut = new MapProxyConsolidator();
        var actual = sut.consolidate( Set.of(mbt1, mbt2, mbt3, mbt4));
        
        assertTrue("Expected Success, got " + actual, actual instanceof Success );
        assertEquals(
            Map.of("b","B", "c","C", "x","X"),
            ((Success)actual).apply(baseMap)
        );
    }
    
    @Test
    public void testConflictingWrites() {
        // need to test both deletes and puts
        MapProxy mp1 = new MapProxy(baseMap);
        mp1.put("x", "X1");
        
        MapProxy mp2 = new MapProxy(baseMap);
        mp2.put("x", "X2");
        
        MockBThreadSyncSnapshot mbt1 = TestUtils.makeMockBtss();
        mbt1.setBprogramStoreModifications(mp1);
        MockBThreadSyncSnapshot mbt2 = TestUtils.makeMockBtss();
        mbt2.setBprogramStoreModifications(mp2);
        
        var sut = new MapProxyConsolidator();
        var actual = sut.consolidate( Set.of(mbt1, mbt2 ));
        
        assertTrue( actual instanceof Conflict );
        var cfl = (Conflict)actual;
        assertEquals( "x", cfl.conflicts.keySet().iterator().next() );
    }
    
    @Test
    public void testConflictingWriteAndDelete() {
        MapProxy mp1 = new MapProxy(baseMap);
        mp1.put("b", "B1");
        
        MapProxy mp2 = new MapProxy(baseMap);
        mp2.remove("b");
        
        MockBThreadSyncSnapshot mbt1 = TestUtils.makeMockBtss();
        mbt1.setBprogramStoreModifications(mp1);
        MockBThreadSyncSnapshot mbt2 = TestUtils.makeMockBtss();
        mbt2.setBprogramStoreModifications(mp2);
        
        var sut = new MapProxyConsolidator();
        var actual = sut.consolidate( Set.of(mbt1, mbt2 ));
        
        assertTrue( actual instanceof Conflict );
        var cfl = (Conflict)actual;
        assertEquals( "b", cfl.conflicts.keySet().iterator().next() );
    }
}
