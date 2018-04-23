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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import java.util.HashSet;
import java.util.Set;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 *
 * A {@link VisitedStateStore} that stores the state of the {@code BThread}s in the node.
 * Ignores the last event that led to this node.
 * 
 * @author michael
 */
public class BThreadSnapshotVisitedStateStore implements VisitedStateStore {
    private final Set<Set<BThreadSyncSnapshot>> visited = new HashSet<>();
    
    @Override
    public void store(Node nd) {
        ContextFactory.getGlobal().call( 
            (_cx)->visited.add( nd.getSystemState().getBThreadSnapshots() )
        );
    }

    @Override
    public boolean isVisited(Node nd) {
        return (Boolean)(ContextFactory.getGlobal().call( 
            (_cx)-> visited.contains( nd.getSystemState().getBThreadSnapshots() )
        ));
        
    }   
    
    @Override
    public void clear() {
        visited.clear();
    }
    
    @Override
    public String toString() {
        return "[BThreadSnapshotVisitedStateStore visited:" + visited.size()+ ']';
    }
}
