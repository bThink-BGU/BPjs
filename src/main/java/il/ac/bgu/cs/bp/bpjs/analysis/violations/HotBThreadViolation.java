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
import java.util.Set;
import static java.util.stream.Collectors.joining;

/**
 *
 * A violation where a b-thread can get into an infinite loop where all its
 * sync points are hot. This class can report on more than a single b-thread
 * having such violation, by using a {@code Set<String>} containing b-thread
 * names.
 * 
 * @author michael
 */
public class HotBThreadViolation extends LivenessViolation {
    
    private final Set<String> bthreadNames;
    
    public HotBThreadViolation(ExecutionTrace trace, Set<String> someBThreadNames) {
        super(trace);
        this.bthreadNames = someBThreadNames;
    }
    
    @Override
    public String decsribe() {
        return "Hot run violation: b-threads {"
            + (bthreadNames.stream().collect(joining(" ,"))) 
            + "} can each get to an infinite hot loop. Cycle returns to index " + getCounterExampleTrace().getCycleToIndex()
            + " because of event " + getCycleToEvent(); 
    }

    public Set<String> getBThreadNames() {
        return bthreadNames;
    }
    
}
