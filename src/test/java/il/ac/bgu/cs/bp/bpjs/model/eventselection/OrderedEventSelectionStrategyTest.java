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

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
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
    
    private static final BEvent EVT_1 = new BEvent("evt1");
    private static final BEvent EVT_2 = new BEvent("evt2");
    private static final BEvent EVT_3 = new BEvent("evt3");
    private static final BEvent EVT_4 = new BEvent("evt4");
    
    /**
     * Test of selectableEvents method, of class OrderedEventSelectionStrategy.
     */
    @Test
    public void testSelectableEvents_noBlocking() {
        
       OrderedEventSelectionStrategy sut = new OrderedEventSelectionStrategy();
       
       BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
           new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1, EVT_2, EVT_3, EVT_4))),
           new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2, EVT_3, EVT_4)))
       );
       
       assertEquals( new HashSet<>(Arrays.asList(EVT_1, EVT_2)), sut.selectableEvents(bpss));
    }

    @Test
    public void testSelectableEvents_withBlocking() {
        
       OrderedEventSelectionStrategy sut = new OrderedEventSelectionStrategy();
       
       BProgramSyncSnapshot bpss = TestUtils.makeBPSS(
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_1, EVT_2, EVT_3, EVT_4))),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_2, EVT_3, EVT_4))),
            new MockBThreadSyncSnapshot(SyncStatement.make().request(Arrays.asList(EVT_3, EVT_4)).block(EVT_1))
       );
       
       assertEquals( new HashSet<>(Arrays.asList(EVT_3, EVT_2)), sut.selectableEvents(bpss));
    }
    
}
