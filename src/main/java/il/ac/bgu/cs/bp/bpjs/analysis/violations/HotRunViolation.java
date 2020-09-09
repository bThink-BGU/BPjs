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
import java.util.Set;
import static java.util.stream.Collectors.joining;

/**
 * 
 * A violation where a set of b-threads can get into an infinite loop where,
 * at east sync point, at least one of them is hot.
 * 
 * @author michael
 */
public class HotRunViolation extends LivenessViolation {
    
    private final Set<String> bThreadNames;
    
    public HotRunViolation(ExecutionTrace counterExampleTrace, Set<String> someBThreadNames) {
        super(counterExampleTrace);
        bThreadNames = someBThreadNames;
    }

    @Override
    public String decsribe() {
        return String.format("Hot run of b-threads {%s}: returning to index %d in the trace because of event %s", 
            bThreadNames.stream().sorted().collect(joining(", ")),
            getCycleToIndex(),
            getCycleToEvent()
        );
    }

    public Set<String> getBThreadNames() {
        return bThreadNames;
    }

}
