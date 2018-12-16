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

import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import java.util.List;
import java.util.Optional;

/**
 * Detects illegal states during a DFS verification. Instances are used by
 * {@link DfsBProgramVerifier} to detect deadlocks, assertion failures, etc.
 * 
 * @see il.ac.bgu.cs.bp.bpjs.analysis.DefaultDfsVerificationInspections
 * 
 * @author michael
 */
public interface DfsVerificationInspection {
    
    /**
     * Inspects the current trace for violations.
     * @param currentTrace The trace of the b-program, up to the current point.
     * @return A non-empty optional with the violation details, or an empty 
     *         optional, if everything is fine.
     */
    Optional<Violation> inspectTrace( List<DfsTraversalNode> currentTrace );
    
    /**
     * Inspects a cycle in the program graph for possible violations.
     * 
     * For example, in the following graph:
     * <code>
     * [a]-[b]-[c]-[d]--...
     *       \      |
     *        +-----+
     * </code>
     * 
     * This methods will be called with trace={@code [a][b][c][d]} and 
     * cyclicPass={@code [b]}.
     * 
     * @param currentTrace Program trace up to this point.
     * @param cyclicPass   A node in the current trace the cycle returns to.
     * @return A non-empty optional with the violation details, or an empty 
     *         optional, if everything is fine.
     */
    Optional<Violation> inspectCycle( List<DfsTraversalNode> currentTrace, DfsTraversalNode cyclicPass);
}
