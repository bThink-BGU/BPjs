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

import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * BSyncStatement is tested thoroughly throughout the general test suite. Here we only add
 * parts that are not covered by other tests.
 * 
 * @author michael
 */
public class BSyncStatementTest {
    
    private final BEvent eventOne = new BEvent("one");
    private final BEvent eventTwo = new BEvent("two");
    private final BEvent eventTri = new BEvent("tri");
    
    
    @Test
    public void testEquals() {
        BSyncStatement sut = BSyncStatement.make();
        
        // Sanity
        assertTrue( sut.equals(sut) );
        assertFalse( sut.equals(null) );
        assertFalse( sut.equals((Object)"abc") );
        
        // different requests
        Assert.assertNotEquals( sut.request(eventOne), sut.request(eventTwo) );
        Assert.assertEquals( sut.request(eventTri), sut.request(eventTri) );
        
        // different interrupt
        Assert.assertNotEquals( sut.interrupt(eventOne), sut.interrupt(eventTwo) );
        Assert.assertEquals( sut.interrupt(eventTri), sut.interrupt(eventTri) );
    }
    
}
