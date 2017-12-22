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
package il.ac.bgu.cs.bp.bpjs.verification.requirements;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.Node;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

class TestingNode extends Node {
    TestingNode( BEvent e ) {
        super(null, null, e);
    }
}

/**
 *
 * @author michael
 */
public class EventDetectorTest {
    
    private static final BEvent NOT_THIS = new BEvent("not-this");
    private static final BEvent YES_THIS = new BEvent("YES-this");
            
    /**
     * Test of checkConformance method, of class EventDetector.
     */
    @Test
    public void testCheckConformance() {
        EventNotPresent sut = new EventNotPresent(YES_THIS);
        List<Node> trace = Arrays.asList( NOT_THIS, NOT_THIS, NOT_THIS, YES_THIS )
                .stream().map(e ->  new TestingNode(e) ).collect( toList() );
        
        assertFalse( sut.checkConformance(trace) );
        
        trace.remove(trace.size()-1);
        
        assertTrue( sut.checkConformance(trace) );
    }
    
}
