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
package il.ac.bgu.cs.bp.bpjs.analysis.listeners;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import java.io.PrintStream;

/**
 * Logs the state of the verifier with short, not too-detailed messages.
 * @author michael
 */
public class PrintDfsVerifierListener implements DfsBProgramVerifier.ProgressListener {
    
    private final PrintStream out;

    public PrintDfsVerifierListener(PrintStream out) {
        this.out = out;
    }

    public PrintDfsVerifierListener() {
        this(System.out);
    }

    @Override
    public void started(DfsBProgramVerifier v) {
        out.println("/v/ verification of '" + v.getCurrentBProgram().getName() + "' started");
    }

    @Override
    public void iterationCount(long count, long statesHit, DfsBProgramVerifier v) {
        out.println("/v/ " + v.getCurrentBProgram().getName() + ": iterations: " + count + " statesHit: " + statesHit);
        
    }

    @Override
    public void maxTraceLengthHit(ExecutionTrace trace, DfsBProgramVerifier v) {
        out.println("/v/ " + v.getCurrentBProgram().getName() + ": hit max trace length.");
    }

    @Override
    public void done(DfsBProgramVerifier v) {
        out.println("/v/ verification of " + v.getCurrentBProgram().getName() + " done");
    }

    @Override
    public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
        out.println("/v/ Violation found: " + aViolation.decsribe() );
        return false;
    }
    
}
