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
package il.ac.bgu.cs.bp.bpjs.mains;

import il.ac.bgu.cs.bp.bpjs.analysis.BThreadSnapshotVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotBProgramCycleViolation;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import java.util.Objects;
import static java.util.stream.Collectors.joining;

/**
 *
 * @author michael
 */
public class CaseofTheHotFruitBProgram {
    
    public static void main(String[] args) throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("DFSVerifierTests/fruitRatio.js");
//        run(bprog);
        verify(bprog);
    }
    
    public static void run(BProgram bprog) {
        BProgramRunner rnr = new BProgramRunner(bprog);
        rnr.addListener(new PrintBProgramRunnerListener());
        
        rnr.run();
    }
    
    public static void verify(BProgram bprog) throws Exception {

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        // Adding the inspector means it will be the only inspector run, as the default set will not be used.
        vfr.addInspector(DfsInspections.HotBProgramCycles);
        vfr.setVisitedNodeStore( new BThreadSnapshotVisitedStateStore() );
        final VerificationResult res = vfr.verify(bprog);

        System.out.println("Violation found: " + res.isViolationFound());
        System.out.println("Visited state count: " + res.getScannedStatesCount());
        res.getViolation().ifPresent( v -> {
            System.out.println(v.decsribe());
            System.out.println("Trace:");
            HotBProgramCycleViolation hcv = (HotBProgramCycleViolation) v;
            System.out.println(hcv.getCounterExampleTrace().stream()
                                    .map(nd->nd.getLastEvent())
                                    .map(e -> Objects.toString(e))
                                    .collect(joining("\n")));
            System.out.println("Cycle-to Index:" + hcv.getCycleToIndex());
            System.out.println("Cycle-to Event:" + hcv.getCycleToEvent());
        });
        
        
    }
    
}
