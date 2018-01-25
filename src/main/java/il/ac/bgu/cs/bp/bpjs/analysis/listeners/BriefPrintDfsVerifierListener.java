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

import il.ac.bgu.cs.bp.bpjs.analysis.Node;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import java.io.PrintStream;
import java.util.List;

/**
 * Logs the state of the verifier with short, not too-detailed messages.
 * @author michael
 */
public class BriefPrintDfsVerifierListener implements DfsBProgramVerifier.ProgressListener {
    
    private final PrintStream out;

    public BriefPrintDfsVerifierListener(PrintStream out) {
        this.out = out;
    }

    public BriefPrintDfsVerifierListener() {
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
    public void maxTraceLengthHit(List<Node> trace, DfsBProgramVerifier v) {
        out.println("/v/ " + v.getCurrentBProgram().getName() + ": hit max trace length.");
    }

    @Override
    public void done(DfsBProgramVerifier v) {
        out.println("/v/ verification of " + v.getCurrentBProgram().getName() + " done");
    }
    
}
