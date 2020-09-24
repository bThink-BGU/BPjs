/*
 * The MIT License
 *
 * Copyright 2020 michael.
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

/**
 * A violation that requires infinite time to manifest, such as a b-program
 * getting into an infinite loop which prevents it from achieving a required 
 * goal. These types of violations happen because of a "bad" cycle the the 
 * violating b-program's state graph. The exact definition of "bad" varies,
 * hence the "abstract base class with concrete sub-classes" design.
 * 
 * 
 * @author michael
 */
public abstract class LivenessViolation extends Violation {
    
    public LivenessViolation(ExecutionTrace counterExampleTrace) {
        super(counterExampleTrace);
    }

    public BEvent getCycleToEvent() {
        List<ExecutionTrace.Entry> nodes = getCounterExampleTrace().getNodes();
        return nodes.get(nodes.size() - 1).getEvent().get();
    }

    public int getCycleToIndex() {
        return getCounterExampleTrace().getCycleToIndex();
    }
    
}
