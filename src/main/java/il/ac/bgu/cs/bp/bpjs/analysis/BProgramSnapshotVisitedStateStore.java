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

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * A {@link VisitedStateStore} that stores the state of the {@code BThread}s in the node.
 * Ignores the last event that led to this node.
 *
 * @author achiya
 */
public class BProgramSnapshotVisitedStateStore implements VisitedStateStore {
    private final Set<Snapshot> visited = new HashSet<>();
    
    /**
     * The item that's stored in {@link #visited}.
     */
    private static final class Snapshot {
        final BProgramSyncSnapshot bpss;
        private final int hashCode;
    
        Snapshot( BProgramSyncSnapshot bpss ){
            this.bpss = bpss;
            hashCode = bpss.hashCode();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            
            final Snapshot other = (Snapshot) obj;
            return (hashCode==other.hashCode)
                    && bpss.equals(other.bpss);
        }
    }
    
    @Override
    public void store(BProgramSyncSnapshot bss) {
        visited.add(new Snapshot(bss));
    }

    @Override
    public boolean isVisited(BProgramSyncSnapshot bss) {
        return visited.contains(new Snapshot(bss));
    }   
    
    @Override
    public void clear() {
        visited.clear();
    }

    @Override
    public long getVisitedStateCount() {
        return visited.size();
    }
    
    @Override
    public String toString() {
        return "[BProgramSnapshotVisitedStateStore visited:" + visited.size()+ ']';
    }
}
