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

import il.ac.bgu.cs.bp.bpjs.analysis.violations.DeadlockViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.FailedAssertionViolation;
import il.ac.bgu.cs.bp.bpjs.model.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * ,
 *
 * @author michael
 */
public class VerificationResultOptionsTest {

    @Test
    public void testOKProgram() throws Exception {
        final SingleResourceBProgram bprog = new SingleResourceBProgram("DFSVerifierTests/VerificationResultOptions.js");

        bprog.putInGlobalScope("addWaiter", false);
        bprog.putInGlobalScope("createDeadlock", false);
        bprog.putInGlobalScope("createFailedAssertion", false);

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        final VerificationResult res = vfr.verify(bprog);

        assertFalse(res.isViolationFound());
    }

    @Test
    public void testDeadlockedProgram() throws Exception {
        final SingleResourceBProgram bprog = new SingleResourceBProgram("DFSVerifierTests/VerificationResultOptions.js");

        bprog.putInGlobalScope("addWaiter", false);
        bprog.putInGlobalScope("createDeadlock", true);
        bprog.putInGlobalScope("createFailedAssertion", false);

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DeadlockViolation);
    }

    @Test
    public void testViolatingProgram() throws Exception {
        final SingleResourceBProgram bprog = new SingleResourceBProgram("DFSVerifierTests/VerificationResultOptions.js");

        bprog.putInGlobalScope("addWaiter", false);
        bprog.putInGlobalScope("createDeadlock", false);
        bprog.putInGlobalScope("createFailedAssertion", true);

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof FailedAssertionViolation);
        
        FailedAssertion expectedAssert = new FailedAssertion("B happened", "assertor");
        assertEquals(expectedAssert, ((FailedAssertionViolation)res.getViolation().get()).getFailedAssertion());
    }

    @Test
    public void testWaitingIsNotDeadlock() throws Exception {
        final SingleResourceBProgram bprog = new SingleResourceBProgram("DFSVerifierTests/VerificationResultOptions.js");

        bprog.putInGlobalScope("addWaiter", true);
        bprog.putInGlobalScope("createDeadlock", false);
        bprog.putInGlobalScope("createFailedAssertion", false);

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        final VerificationResult res = vfr.verify(bprog);

        assertFalse(res.isViolationFound());
    }

    @Test
    public void testImmediateAssert() throws Exception {
        BProgram bprog = new SingleResourceBProgram("ImmediateAssert.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof FailedAssertionViolation);
        FailedAssertion expected = new FailedAssertion("failRightAWay!", "forward");
        assertEquals(expected, ((FailedAssertionViolation)res.getViolation().get()).getFailedAssertion());
        assertEquals(0, res.getScannedStatesCount());
    }


}
