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

import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class OrderedEventSelectionStrategyTest {
    
    public OrderedEventSelectionStrategyTest() {
    }
    
    private static final BEvent evt1 = new BEvent("evt1");
    private static final BEvent evt2 = new BEvent("evt2");
    private static final BEvent evt3 = new BEvent("evt3");
    private static final BEvent evt4 = new BEvent("evt4");
    
    /**
     * Test of selectableEvents method, of class OrderedEventSelectionStrategy.
     */
    @Test
    public void testSelectableEvents_noBlocking() {
        
       OrderedEventSelectionStrategy sut = new OrderedEventSelectionStrategy();
       
       Set<BSyncStatement> stmts = new HashSet<>();
       stmts.add(BSyncStatement.make().request(Arrays.asList(evt1, evt2, evt3, evt4)));
       stmts.add(BSyncStatement.make().request(Arrays.asList(evt2, evt3, evt4)));
       
       assertEquals( new HashSet<>(Arrays.asList(evt1, evt2)), sut.selectableEvents(stmts, Collections.emptyList()));
    }

    @Test
    public void testSelectableEvents_withBlocking() {
        
       OrderedEventSelectionStrategy sut = new OrderedEventSelectionStrategy();
       
       Set<BSyncStatement> stmts = new HashSet<>();
       stmts.add(BSyncStatement.make().request(Arrays.asList(evt1, evt2, evt3, evt4)));
       stmts.add(BSyncStatement.make().request(Arrays.asList(evt2, evt3, evt4)));
       stmts.add(BSyncStatement.make().request(Arrays.asList(evt3, evt4)).block(evt1));
       
       assertEquals( new HashSet<>(Arrays.asList(evt3, evt2)), sut.selectableEvents(stmts, Collections.emptyList()));
    }
    
}
