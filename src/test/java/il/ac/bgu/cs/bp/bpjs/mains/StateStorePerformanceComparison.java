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
package il.ac.bgu.cs.bp.bpjs.mains;

import il.ac.bgu.cs.bp.bpjs.analysis.*;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;

/**
 * This program runs the DFS verifier against the same maze using different
 * {@link VisitedStateStore} implementations, and prints the timing results
 * to the console.
 * <p>
 * Run this test as follows:
 * <ul>
 * <li>Build the BPjs uber-jar: {@code mvn package -P uber-jar}</li>
 * <li>Build the BPjs tests-jar: {@code mvn jar:test-jar}</li>
 * <li>Run and collect output: {@code java -Xmx4G -cp [uber jar]:[tests jar] il.ac.bgu.cs.bp.bpjs.analysis.DFSVerifierTests.StateStorePerformanceComparison}</li>
 * </ul>
 *
 * @author michael
 */
public class StateStorePerformanceComparison {

    private final static String IMPLEMENTATION = "MazesNegative.js";
    private final static BEvent TARGET_FOUND_EVENT = BEvent.named("targetFound");
    private static final int ITERATIONS = 100;
    private static final int HEAT_UP_ITERATIONS = 10;
    private static String MAZE_NAME = "complex2";

    public static void main(String[] args) throws Exception {

        if (args.length == 1) {
            MAZE_NAME = args[0];
        }

        // prepare verifier
        DfsBProgramVerifier verifier = new DfsBProgramVerifier();
        verifier.setDetectDeadlocks(false);

        // test
        verifier.setVisitedNodeStore(new BThreadSnapshotVisitedStateStore());
        runVerifier(verifier);
        verifier.setVisitedNodeStore(new HashVisitedStateStore());
        runVerifier(verifier);
        verifier.setVisitedNodeStore(new ForgetfulVisitedStateStore());
        runVerifier(verifier);

    }

    private static BProgram makeBProgram() {
        // prepare b-program
        final BProgram bprog = new SingleResourceBProgram(IMPLEMENTATION);
        bprog.putInGlobalScope("MAZE_NAME", MAZE_NAME);
        bprog.putInGlobalScope("TARGET_FOUND_EVENT", TARGET_FOUND_EVENT);
        bprog.appendSource(Requirements.eventNotSelected(TARGET_FOUND_EVENT.getName()));
        return bprog;
    }

    private static void runVerifier(DfsBProgramVerifier vfr) throws Exception {
        System.out.println("Testing " + vfr.getVisitedNodeStore());
        System.out.println("Heating up");
        for (int i = 0; i < HEAT_UP_ITERATIONS; i++) {
            vfr.verify(makeBProgram());
        }
        for (int i = 0; i < ITERATIONS; i++) {
            VerificationResult res = vfr.verify(makeBProgram());
            System.out.println(res.getTimeMillies());
        }

    }
}
