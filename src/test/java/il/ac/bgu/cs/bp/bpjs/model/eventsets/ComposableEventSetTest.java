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
package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import static il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet.*;
import static java.util.Arrays.asList;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class ComposableEventSetTest {
    
    public ComposableEventSetTest() {
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
    
    
    static final BEvent E_A = new BEvent("A");
    static final BEvent E_B = new BEvent("B");
    static final BEvent E_C = new BEvent("C");
    static final BEvent E_D = new BEvent("D");
    
    /**
     * Test of theEventSet method, of class ComposableEventSet.
     */
    @Test
    public void testTheEventSet() {
        EventSet sutA = theEventSet(E_A);
        EventSet sutA1 = theEventSet(E_A);
        EventSet sutB = theEventSet(E_B);
        
        assertEquals( sutA, sutA );
        assertEquals( sutA, sutA1 );
        assertNotEquals( sutA, sutB );
        
        assertEquals( sutA.hashCode(), sutA.hashCode() );
        assertEquals( sutA.hashCode(), sutA1.hashCode() );
        assertNotEquals( sutA.hashCode(), sutB.hashCode() );
        
        assertTrue( sutA.toString().contains( E_A.toString() ));
    }

    /**
     * Test of not method, of class ComposableEventSet.
     */
    @Test
    public void testNot() {
        EventSet sut = not(theEventSet(E_A));
        assertFalse( sut.contains(E_A) );
        assertTrue( sut.contains(E_B) );
        assertEquals( sut, not(not(sut)) );
        assertEquals( sut, not(E_A) );
        
        assertTrue( sut.toString().contains( E_A.toString() ));
        assertTrue( sut.toString().contains( "not" ));
        
        assertFalse( sut.equals(new Object()) );
    }

    /**
     * Test of anyOf method, of class ComposableEventSet.
     */
    @Test
    public void testAnyOf_Collection() {
        EventSet sut = anyOf( asList(E_A, E_B) );
        assertTrue( sut.contains(E_A) );
        assertTrue( sut.contains(E_B) );
        assertFalse( sut.contains(E_C) );
        
        assertTrue( sut.toString().contains(E_A.toString()) );
        assertTrue( sut.toString().contains(E_B.toString()) );
        assertTrue( sut.toString().contains("anyOf") );
        
        HashSet<EventSet> ess = new HashSet<>();
        ess.add( anyOf(asList(E_A, E_B)) );
        ess.add( anyOf(asList(E_B, E_A)) );
        ess.add( anyOf(asList(E_B, E_A, E_B)) );
        ess.add( anyOf(asList(E_B, E_A, E_B, E_A, E_A)) );
        
        assertEquals( 1, ess.size() );
    }

    /**
     * Test of anyOf method, of class ComposableEventSet.
     */
    @Test
    public void testAnyOf_EventSetArr() {
        EventSet sut = anyOf( E_A, E_B );
        assertTrue( sut.contains(E_A) );
        assertTrue( sut.contains(E_B) );
        assertFalse( sut.contains(E_C) );
        
        assertTrue( sut.toString().contains(E_A.toString()) );
        assertTrue( sut.toString().contains(E_B.toString()) );
        assertTrue( sut.toString().contains("anyOf") );
        
        HashSet<EventSet> ess = new HashSet<>();
        ess.add( anyOf(E_A, E_B) );
        ess.add( anyOf(E_B, E_A) );
        ess.add( anyOf(E_B, E_A, E_B) );
        ess.add( anyOf(E_B, E_A, E_B, E_A, E_A) );
        
        assertEquals( 1, ess.size() );
    }

    /**
     * Test of allOf method, of class ComposableEventSet.
     */
    @Test
    public void testAllOf_Collection() {
        EventSet aOrB = anyOf( E_A, E_B );
        EventSet bOrC = anyOf( E_B, E_C );
        EventSet sut = allOf( asList(aOrB, bOrC) );
        assertFalse( sut.contains(E_A) );
        assertTrue( sut.contains(E_B) );
        assertFalse( sut.contains(E_C) );
        
        assertTrue( sut.toString().contains(E_B.toString()) );
        assertTrue( sut.toString().contains("allOf") );
        
        HashSet<EventSet> ess = new HashSet<>();
        ess.add( allOf(asList(aOrB, bOrC)) );
        ess.add( allOf(asList(bOrC, aOrB)) );
        ess.add( allOf(asList(bOrC, aOrB,bOrC, aOrB,bOrC, aOrB)) );

        assertEquals( 1, ess.size() );
    }

    /**
     * Test of allOf method, of class ComposableEventSet.
     */
    @Test
    public void testAllOf_EventSetArr() {
        EventSet aOrB = anyOf( E_A, E_B );
        EventSet bOrC = anyOf( E_B, E_C );
        EventSet sut = allOf( aOrB, bOrC );
        assertFalse( sut.contains(E_A) );
        assertTrue( sut.contains(E_B) );
        assertFalse( sut.contains(E_C) );
        
        assertTrue( sut.toString().contains(E_B.toString()) );
        assertTrue( sut.toString().contains("allOf") );
        
        HashSet<EventSet> ess = new HashSet<>();
        ess.add( allOf(aOrB, bOrC) );
        ess.add( allOf(bOrC, aOrB) );
        ess.add( allOf(bOrC, aOrB,bOrC, aOrB,bOrC, aOrB) );

        assertEquals( 1, ess.size() );
    }

    /**
     * Test of and method, of class ComposableEventSet.
     */
    @Test
    public void testAnd() {
        ComposableEventSet aOrB = anyOf( E_A, E_B );
        ComposableEventSet bOrC = anyOf( E_B, E_C );
        
        ComposableEventSet sut = aOrB.and(bOrC);
        
        assertTrue( sut.contains(E_B) );
        assertFalse( sut.contains(E_A) );
        assertFalse( sut.contains(E_C) );
        
        assertEquals( aOrB.and(bOrC), bOrC.and(aOrB) );
        
        HashSet<EventSet> same = new HashSet<>();
        same.add(theEventSet(E_A).and(E_B).and(E_C));
        same.add(theEventSet(E_C).and(E_A).and(E_B));
        same.add(theEventSet(E_C).and(E_A).and(E_B).and(E_A));
        assertEquals( 1, same.size() );
        
        assertFalse( sut.equals(new Object()) );
    }

    /**
     * Test of or method, of class ComposableEventSet.
     */
    @Test
    public void testOr() {
        ComposableEventSet sutA = theEventSet(E_A).or(E_B);
        ComposableEventSet sutB = theEventSet(E_B).or(E_A);
        
        assertEquals( sutA, sutB );
        assertEquals( sutA, sutB.or(E_A) );
    }

    /**
     * Test of xor method, of class ComposableEventSet.
     */
    @Test
    public void testXor() {
        ComposableEventSet aOrB = anyOf( E_A, E_B );
        ComposableEventSet bOrC = anyOf( E_B, E_C );
        EventSet sut = aOrB.xor(bOrC);
        
        assertTrue( sut.contains(E_A) );
        assertFalse( sut.contains(E_B) );
        assertTrue( sut.contains(E_C) );
        assertFalse( sut.contains(E_D) );
        
        assertTrue( sut.toString().contains("xor") );
        assertTrue( sut.toString().contains(aOrB.toString()) );
        assertTrue( sut.toString().contains(bOrC.toString()) );
        
        HashSet<EventSet> sets = new HashSet<>();
        
        sets.add( aOrB.xor(bOrC) );
        sets.add( bOrC.xor(aOrB) );
        sets.add( bOrC.xor(E_A) );
        assertEquals(2, sets.size() );
        
        assertFalse( sut.equals(new Object()) );
    }

    /**
     * Test of nor method, of class ComposableEventSet.
     */
    @Test
    public void testNor() {
        ComposableEventSet aOrB = anyOf( E_A, E_B );
        ComposableEventSet bOrC = anyOf( E_B, E_C );
        EventSet sut = aOrB.nor(bOrC);
        
        assertFalse( sut.contains(E_A) );
        assertFalse( sut.contains(E_B) );
        assertFalse( sut.contains(E_C) );
        assertTrue( sut.contains(E_D) );
        
        assertTrue( sut.toString().contains("not") );
        assertTrue( sut.toString().contains("any") );
        assertTrue( sut.toString().contains(aOrB.toString()) );
        assertTrue( sut.toString().contains(bOrC.toString()) );
        
        assertFalse( sut.equals(new Object()) );
    }

    /**
     * Test of nand method, of class ComposableEventSet.
     */
    @Test
    public void testNand() {
        ComposableEventSet aOrB = anyOf( E_A, E_B );
        ComposableEventSet bOrC = anyOf( E_B, E_C );
        EventSet sut = aOrB.nand(bOrC);
        
        assertTrue( sut.contains(E_A) );
        assertFalse( sut.contains(E_B) );
        assertTrue( sut.contains(E_C) );
        assertTrue( sut.contains(E_D) );
        
        assertTrue( sut.toString().contains("not") );
        assertTrue( sut.toString().contains("all") );
        assertTrue( sut.toString().contains(aOrB.toString()) );
        assertTrue( sut.toString().contains(bOrC.toString()) );
    }
    
    @Test
    public void testSemantics() {
        EventSet sutA = theEventSet(E_A).or(E_B).and( theEventSet(E_B).or(E_C) );
        EventSet sutB = theEventSet(E_C).or(E_B).and( theEventSet(E_B).or(E_A) );
        assertEquals( sutA, sutB );
        
        assertEquals( allOf(E_A, E_B, E_C, E_D), allOf(E_A, E_B).and(allOf(E_C, E_D)) );
        assertEquals( allOf(E_A, E_B, E_C, E_D), allOf(E_A, E_B, E_B, E_B).and(allOf(E_C, E_D)) );
        
        assertEquals( anyOf(E_A, E_B, E_C, E_D), anyOf(E_A, E_B).or(anyOf(E_C, E_D)) );
        assertEquals( anyOf(E_A, E_B, E_C, E_D), anyOf(E_A, E_B, E_B, E_B).or(anyOf(E_C, E_D)) );
        
        assertTrue( theEventSet(E_A).xor(E_B).or(E_C).contains(E_A) );
        assertTrue( theEventSet(E_A).xor(E_B).or(E_C).contains(E_B) );
        assertTrue( theEventSet(E_A).xor(E_B).or(E_C).contains(E_C) );
    }
    
}
