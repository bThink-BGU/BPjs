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
package il.ac.bgu.cs.bp.bpjs.verification.requirements;

import il.ac.bgu.cs.bp.bpjs.search.Node;
import java.util.List;

/**
 * A requirement for execution paths. The {@link #checkConformance(java.util.List)} method returns {@code true} 
 * when the execution path complies with the implemented requirement, and {@code false} otherwise.
 * 
 * @author michael
 */
public interface PathRequirement {
    
    String getName();
    
    /**
     * Test that {@code trace} conforms to the implemented requirement.
     * @param trace the BProgram trace thus far. Immutable.
     * @return {@code true} iff the trace conforms to the requirement {@code this} implements; {@code false} otherwise.
     */
    boolean checkConformance( List<Node> trace );
    
}
