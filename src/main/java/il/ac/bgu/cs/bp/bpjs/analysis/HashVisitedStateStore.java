/*
 * The MIT License
 *
 * Copyright 2018 michael.
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
import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link VisitedStateStore} that uses a hash function over the BProgram's states.
 * @author michael
 */
public class HashVisitedStateStore implements VisitedStateStore {
    private final Set<Long> visited = new TreeSet<>();

    @Override
    public void store(Node nd) {
        visited.add( hash(nd.getSystemState().getBThreadSnapshots()) );
    }

    @Override
    public boolean isVisited(Node nd) {
        return visited.contains( hash(nd.getSystemState().getBThreadSnapshots()) );
    }

    private long hash( Set<BThreadSyncSnapshot> snapshots ) {
        long hash = 0;
        for ( BThreadSyncSnapshot snp : snapshots ) {
            hash = hash ^ snp.getContinuationProgramState().hashCode();
        }
        return hash;
    }

    @Override
    public void clear() {
        visited.clear();
    }

    @Override
    public String toString() {
        return "[HashVisitedStateStore stateCount:" + visited.size()+ ']';
    }
}
