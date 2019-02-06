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
package il.ac.bgu.cs.bp.bpjs.analysis.violations;

import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.joining;

/**
 *
 * A violation where a set of one or more b-threads can be caught in an infinite
 * loop where all their sync points are hot.
 * 
 * @author michael
 */
public class HotBThreadCycleViolation extends Violation {
    
    private final Set<String> bthreads;
    
    public HotBThreadCycleViolation(ExecutionTrace trace, Set<String> bthreads) {
        super(trace);
        this.bthreads = bthreads;
    }
    
    @Override
    public String decsribe() {
        return "Hot b-thread cycle violation: b-threads "
            + (bthreads.stream().collect(joining(" ,"))) 
            + " can get to an infinite hot loop. Cycle returns to index " + getCounterExampleTrace().getCycleToIndex()
                + " because of event " + getCycleToEvent(); 
    }

    public int getCycleToIndex() {
        return getCounterExampleTrace().getCycleToIndex();
    }

    public BEvent getCycleToEvent() {
        List<ExecutionTrace.Entry> nds = getCounterExampleTrace().getNodes();
        return nds.get(nds.size()-1).getEvent().get();
    }

    public Set<String> getBThreads() {
        return bthreads;
    }
    
}
