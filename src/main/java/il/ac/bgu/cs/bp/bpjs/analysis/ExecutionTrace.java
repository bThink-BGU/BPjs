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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A trace of a single execution of a b-program. Contains the ordered sequence of
 * states and events, and some methods for making common queries about it.
 * 
 * @author michael
 */
public interface ExecutionTrace {
    
    /**
     * A single entry in the trace. Contains a state, and an optional event. 
     * The event may be missing, if the node is the last in the trace (e.g. a 
     * run that got to a dead-end.
     */
    public class Entry {
        private final BProgramSyncSnapshot state;
        private BEvent event;

        public Entry(BProgramSyncSnapshot state, BEvent event) {
            this.state = state;
            this.event = event;
        }

        public Entry( BProgramSyncSnapshot aState ) {
            this(aState, null);
        }
        
        public BProgramSyncSnapshot getState() {
            return state;
        }

        public Optional<BEvent> getEvent() {
            return Optional.ofNullable(event);
        }

        public void setEvent(BEvent event) {
            this.event = event;
        }
        
        @Override
        public String toString() {
            return "[Trace.Entry state:" + state + ", event:" + event + ']';
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.event);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.event, other.event)) {
                return false;
            }
            return Objects.equals(this.state, other.state);
        }
        
    }
    
    /**
     * @return The b-program {@code this} trace belongs to.
     */
    BProgram getBProgram();
    
    /**
     * Number of states in the trace. Note that the trace can be inifinite, so 
     * no point calling this {@code length} or {@code size}.
     * 
     * @return number of states in the trace.
     */
    int getStateCount();
    
    /**
     * Returns the last state in the node list. Note that when a trace is cyclic,
     * this will not be the last state in the actual execution, but rather the last
     * event before returning to a previously visited state.
     * 
     * @return last state in the node list.
     * @see #isCyclic() 
     */
    BProgramSyncSnapshot getLastState();
    
    /**
     * Returns the last event in the node list. For non-cyclic traces, that would
     * be the last event to happen before the current state. In cyclic traces, 
     * this would be the event that causes the b-program to get back to a state
     * already in the trace, thereby closing the cycle.
     * 
     * For traces that are empty, or have a single entry (and thus only last
     * state but no last event), this method returns an empty optional.
     * 
     * @return the last event to happen in the node list, if any.
     */
    Optional<BEvent> getLastEvent();
    
    /**
     * Ordered list of all nodes in the state. In case of cycles, the last node
     * in the list is the last before returning to a previously visited node.
     * 
     * @return list of all nodes in the trace, by order.
     */
    List<Entry> getNodes();
    
    /**
     * @return {@code true} iff the trace ends with a cycle.
     */
    boolean isCyclic();
    
    /**
     * The index of the node in {@link #getNodes} to which the cycle (if any)
     * returns. In case the trace is a-cyclic, returns -1.
     * 
     * @return the index of the return-to node, or -1.
     * @see #isCyclic() 
     */
    int getCycleToIndex();
    
    /**
     * A list of the nodes in the final cycle, or an empty list if the trace
     * is a-cyclic.
     * 
     * @return A list of the nodes in the final cycle.
     */
    List<Entry> getFinalCycle();
}

