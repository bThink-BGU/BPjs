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

import il.ac.bgu.cs.bp.bpjs.analysis.DfsTraversalNode;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.List;

/**
 *
 * @author michael
 */
public class HotBProgramCycleViolation extends Violation {
    
    private final int cycleToIndex;
    private final BEvent event;

    public HotBProgramCycleViolation(List<DfsTraversalNode> counterExampleTrace, int cycleToIndex, BEvent event) {
        super(counterExampleTrace);
        this.cycleToIndex = cycleToIndex;
        this.event = event;
    }

    @Override
    public String decsribe() {
        return "Hot cycle violation: returning to index " + getCycleToIndex() + " in the trace because of event " + event.toString();
    }

    public int getCycleToIndex() {
        return cycleToIndex;
    }

    public BEvent getCycleToEvent() {
        return event;
    }
    
}