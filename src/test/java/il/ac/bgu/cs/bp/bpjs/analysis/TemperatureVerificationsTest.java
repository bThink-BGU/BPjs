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

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotBThreadViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotRunViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotSystemViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotTerminationViolation;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class TemperatureVerificationsTest {
    
    @Test
    public void testHotTermination() throws Exception{
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/hotTerminationExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.addInspection(ExecutionTraceInspections.HOT_TERMINATIONS);
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof HotTerminationViolation);
        
        HotTerminationViolation htv = (HotTerminationViolation) res.getViolation().get();
        assertEquals( htv.getBThreadNames(), Collections.singleton("hotter") );
        System.out.println(htv.decsribe());
    }
    
    
    @Test
    public void testHotSystem_onHotBThreadCycle() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/hotBThreadCycleExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        // Adding the inspector means it will be the only inspector run, as the default set will not be used.
        vfr.addInspection(ExecutionTraceInspections.HOT_SYSTEM);
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof HotSystemViolation);
        HotSystemViolation hcv = (HotSystemViolation) res.getViolation().get();
        System.out.println(hcv.decsribe());
        System.out.println("Trace:");
        System.out.println(TestUtils.traceEventNamesString(hcv.getCounterExampleTrace(), "\n"));
        System.out.println("Cycle-to Index:" + hcv.getCycleToIndex());
        System.out.println("Cycle-to Event:" + hcv.getCycleToEvent());
        assertEquals( "e", hcv.getCycleToEvent().getName() );
        
        // The expected index is the length of the path, minus the cycle length 
        // (which is 3 in our case) plus 1.
        int expectedCycleToIndex = hcv.getCounterExampleTrace().getStateCount()-3;
        assertEquals(expectedCycleToIndex, hcv.getCycleToIndex() );
    }
    
    @Test
    public void testHotSystem_withJsEventSets() throws Exception {
        BProgram bprog = new ResourceBProgram("DFSVerifierTests/fruitRatio.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        // Adding the inspector means it will be the only inspector run, as the default set will not be used.
        vfr.addInspection(ExecutionTraceInspections.HOT_SYSTEM);
        vfr.setVisitedStateStore( new BThreadSnapshotVisitedStateStore() );
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof HotSystemViolation);
        
    }
    
    @Test
    public void testHotBThreadCycle() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/hotBThreadCycleExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        // Adding the inspector means it will be the only inspector run, as the default is to run them all.
        vfr.addInspection(ExecutionTraceInspections.HOT_BTHREADS);
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof HotBThreadViolation);
        HotBThreadViolation htv = (HotBThreadViolation) res.getViolation().get();
        System.out.println(htv.decsribe());
        System.out.println("Trace:");
        System.out.println(TestUtils.traceEventNamesString(htv.getCounterExampleTrace(), "\n"));
        System.out.println("Cycle-to Index:" + htv.getCycleToIndex());
        System.out.println("Cycle-to Event:" + htv.getCycleToEvent());
        assertEquals( "e", htv.getCycleToEvent().getName() );
        assertEquals( new TreeSet<>(Arrays.asList("cycled-thread")), htv.getBThreadNames() );
        
        // The expected index is the length of the path, minus the cycle length 
        // (which is 3 in our case) plus 1.
        int expectedCycleToIndex = htv.getCounterExampleTrace().getStateCount()-3;
        assertEquals(expectedCycleToIndex, htv.getCycleToIndex() );
    }
    
    @Test
    public void testHotSystem_onHotBProgramCycle() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/hotBProgramCycleExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        // Adding the inspector means it will be the only inspector run, as the default is to run them all.
        vfr.addInspection(ExecutionTraceInspections.HOT_SYSTEM);
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof HotSystemViolation);
        HotSystemViolation hcv = (HotSystemViolation) res.getViolation().get();
        assertEquals( "e", hcv.getCycleToEvent().getName() );
        int expectedCycleToIndex = hcv.getCounterExampleTrace().getStateCount()-3;
        assertEquals(expectedCycleToIndex, hcv.getCycleToIndex() );
    }
    
    @Test
    public void testHotBThreadCycle_onHotBProgramCycle() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/hotBProgramCycleExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        // Adding the inspector means it will be the only inspector run, as the default is to run them all.
        vfr.addInspection(ExecutionTraceInspections.HOT_BTHREADS);
        final VerificationResult res = vfr.verify(bprog);

        assertFalse(res.isViolationFound());
    }
    
    @Test
    public void testHotRun_inspectingColdCycle() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/hotRunExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.addInspection(ExecutionTraceInspections.hotRun(Set.of("A=>B", "B=>C")));
        final VerificationResult res = vfr.verify(bprog);

        assertFalse(res.isViolationFound());
    }
    
    @Test
    public void testHotRun_inspectingHotCycle() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/hotRunExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.addInspection(ExecutionTraceInspections.hotRun(Set.of("A=>B", "B=>C", "C=>A")));
        final VerificationResult res = vfr.verify(bprog);
        
        HotRunViolation hrv = (HotRunViolation) res.getViolation().get();
        assertEquals( "a", hrv.getCycleToEvent().getName() );
        assertTrue( res.isViolationFound() );
        assertTrue( res.getViolation().get() instanceof HotRunViolation );
        
        System.out.println(hrv.decsribe());
        System.out.println("Trace:");
        System.out.println(TestUtils.traceEventNamesString(hrv.getCounterExampleTrace(), "\n"));
        System.out.println("Cycle-to Index:" + hrv.getCycleToIndex());
        System.out.println("Cycle-to Event:" + hrv.getCycleToEvent());
    }
    
    public static void main(String[] args) {
        // run the hot cycle b-program, to see that it works.
        final ResourceBProgram bprog = new ResourceBProgram("statementtemp/basicTempTest.js");
        BProgramRunner rnr = new BProgramRunner(bprog);
        
        rnr.addListener(new PrintBProgramRunnerListener());
        
        rnr.run();
    }
}
