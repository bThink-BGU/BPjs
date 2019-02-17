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
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class MultipleViolationsTest {
    
    @Test
    public void testMultipleFailedAssertions() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("DFSVerifierTests/MultipleFailedAssertions.js");
        AtomicInteger violationCount = new AtomicInteger();
        AtomicBoolean maxLengthHit = new AtomicBoolean(false);
        final Set<Violation> violations = new HashSet<>();
        
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.setMaxTraceLength(6);
        
        vfr.setProgressListener( new DfsBProgramVerifier.ProgressListener() {
            @Override public void started(DfsBProgramVerifier vfr) {}

            @Override public void iterationCount(long count, long statesHit, DfsBProgramVerifier vfr) {}

            @Override
            public void maxTraceLengthHit(List<DfsTraversalNode> aTrace, DfsBProgramVerifier vfr) {
                maxLengthHit.set(true);
            }

            @Override
            public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
                int num = violationCount.incrementAndGet();
                System.out.println("Violation " + num + ": " + aViolation.decsribe() );
                violations.add(aViolation);
                return true;
            }

            @Override public void done(DfsBProgramVerifier vfr) {}
        });
        
        final VerificationResult res = vfr.verify(bprog);

        assertFalse( res.isViolationFound() );
        assertEquals( 3, violationCount.get() );
        assertEquals( 3, violations.size() );
        assertTrue( maxLengthHit.get() );
        Violation v = violations.iterator().next();
        assertFalse( v.equals(null) );
        assertFalse( v.equals("Not a Violation") );
    }
    
    @Test
    public void testHotBThreadCycles() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("DFSVerifierTests/MultipleHotCycles.js");
        AtomicInteger violationCount = new AtomicInteger();
        AtomicBoolean maxLengthHit = new AtomicBoolean(false);
        
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.addInspection( ExecutionTraceInspections.HOT_BTHREAD_CYCLES );
        vfr.setMaxTraceLength(6);
        
        vfr.setProgressListener( new DfsBProgramVerifier.ProgressListener() {
            @Override public void started(DfsBProgramVerifier vfr) {}

            @Override public void iterationCount(long count, long statesHit, DfsBProgramVerifier vfr) {}

            @Override
            public void maxTraceLengthHit(List<DfsTraversalNode> aTrace, DfsBProgramVerifier vfr) {
                maxLengthHit.set(true);
            }

            @Override
            public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
                int num = violationCount.incrementAndGet();
                System.out.println("Violation " + num + ": " + aViolation.decsribe() );
                return true;
            }

            @Override public void done(DfsBProgramVerifier vfr) {}
        });
        
        final VerificationResult res = vfr.verify(bprog);

        assertFalse( res.isViolationFound() );
        assertEquals( 2, violationCount.get() );
    }
    
    @Test
    public void testMultipleHotTerminations() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("DFSVerifierTests/MultipleHotTerminations.js");
        AtomicInteger violationCount = new AtomicInteger();
        
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.setMaxTraceLength(6);
        
        vfr.setProgressListener( new DfsBProgramVerifier.ProgressListener() {
            @Override public void started(DfsBProgramVerifier vfr) {}

            @Override public void iterationCount(long count, long statesHit, DfsBProgramVerifier vfr) {}

            @Override
            public void maxTraceLengthHit(List<DfsTraversalNode> aTrace, DfsBProgramVerifier vfr) {}

            @Override
            public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
                int num = violationCount.incrementAndGet();
                System.out.println("Violation " + num + ": " + aViolation.decsribe() );
                return true;
            }

            @Override public void done(DfsBProgramVerifier vfr) {}
        });
        
        final VerificationResult res = vfr.verify(bprog);

        assertFalse( res.isViolationFound() );
        assertEquals( 3, violationCount.get() );

    }
}
