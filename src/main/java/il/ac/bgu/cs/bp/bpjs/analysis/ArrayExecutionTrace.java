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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * An {@link ArrayList}-backed {@link ExecutionTrace} implementation. Allows for
 * relatively efficient runs in stack-based traversals.
 * 
 * @author michael
 */
public class ArrayExecutionTrace implements ExecutionTrace {
    
    private final ArrayList<ExecutionTrace.Entry> stack = new ArrayList<>();
    
    private int cycleToIndex = -1;
    
    private final BProgram bprogram;
    
    private static final int[] BLOOM_KEYS = new int[]{1,13,37};
    /**
     * Bloom filter for exiting BProgramSyncSnapshot. NOT ENTRIES, SNAPSHOTS!
     */
    private final int[] bloomFilter = new int[256];
    
    public ArrayExecutionTrace( BProgram aBProgram ) {
        bprogram = aBProgram;
    }
    
    @Override
    public BProgram getBProgram() {
        return bprogram;
    }
    
    @Override
    public int getStateCount() {
        return stack.size();
    }

    @Override
    public BProgramSyncSnapshot getLastState() {
        return stack.get(stack.size()-1).getState();
    }
    
    @Override
    public BEvent getLastEvent() {
        return stack.get(stack.size()-(isCyclic()?1:2)).getEvent().get();
    }
    
    @Override
    public List<Entry> getNodes() {
        return Collections.unmodifiableList(stack);
    }

    @Override
    public boolean isCyclic() {
        return cycleToIndex != -1;
    }

    @Override
    public int getCycleToIndex() {
        return cycleToIndex;
    }

    @Override
    public List<Entry> getFinalCycle() {
        if ( isCyclic() ) {
            return stack.subList(cycleToIndex, stack.size());
        } else {
            return Collections.emptyList();
        }
    }
    
    public void push( BProgramSyncSnapshot aState ) {
        stack.add( new Entry(aState) );
        int curHash = Objects.hashCode(aState);
        for ( int i=0; i<BLOOM_KEYS.length; i++ ){
            bloomFilter[ Math.abs(curHash*BLOOM_KEYS[i])%bloomFilter.length ]++;
        }
    }
    
    public Entry pop() {
        cycleToIndex = -1;
        Entry onWayOut = stack.remove(stack.size()-1);
        int curHash = Objects.hashCode(onWayOut.getState());
        for ( int i=0; i<BLOOM_KEYS.length; i++ ){
            bloomFilter[ Math.abs(curHash*BLOOM_KEYS[i])%bloomFilter.length ]--;
        } 
        return onWayOut;
    }
    
    public void advance( BEvent anEvent, BProgramSyncSnapshot nextState ){
        stack.get(stack.size()-1).setEvent(anEvent);
        push( nextState );
        cycleToIndex = -1;
    }
    
    public void cycleTo( BEvent anEvent, int aCycleToIndex ) {
        stack.get(stack.size()-1).setEvent(anEvent);
        cycleToIndex = aCycleToIndex;
    }
    
    public int indexOf( BProgramSyncSnapshot bpss ) {
        // check bloom
        int hash = Objects.hashCode(bpss);
        for ( int i=0; i<BLOOM_KEYS.length; i++ ){
            if ( bloomFilter[ Math.abs(hash*BLOOM_KEYS[i])%bloomFilter.length ] == 0 ) {
                return -1;
            }
        }
        
        // if OK, check for real
        for ( int i=0; i<stack.size(); i++ ) {
            if ( stack.get(i).getState().equals(bpss) ) {
                return i;
            }
        }
        return -1;
    }
    
    public void clear() {
        stack.clear();
        cycleToIndex=-1;
        Arrays.fill(bloomFilter, 0);
    }
    
}
