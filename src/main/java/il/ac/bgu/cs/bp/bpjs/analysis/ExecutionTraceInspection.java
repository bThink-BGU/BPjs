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

import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import java.util.Optional;
import java.util.function.Function;

/**
 * A single inspection, detects violations in {@link ExecutionTrace}s.
 * 
 * @author michael
 */
public interface ExecutionTraceInspection {
    
    /**
     * Utility method for composing inspections on the fly.
     * @param aTitle title of the inspection
     * @param f actual inspection function
     * @return an inspection with the title and implementation passed.
     */
    static ExecutionTraceInspection named(String aTitle, Function<ExecutionTrace,Optional<Violation>> f ) {
        return new ExecutionTraceInspection() {
            @Override
            public String title() {
                return aTitle;
            }

            @Override
            public Optional<Violation> inspectTrace(ExecutionTrace aTrace) {
                return f.apply(aTrace);
            }
        };
    }
    
    /**
     * A human-readable title, used for listing and logs.
     * @return inspection title.
     */
    String title();
    
    /**
     * Inspects a trace for violations.
     * @param aTrace The trace of the b-program execution. May be cyclic.
     * @return A non-empty optional with the violation details, or an empty 
     *         optional, if everything is fine.
     */
    Optional<Violation> inspectTrace( ExecutionTrace aTrace );
}
