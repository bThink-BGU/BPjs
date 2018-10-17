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
package il.ac.bgu.cs.bp.bpjs.analysis;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.eventNamesString;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;

import org.junit.Test;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.traceEventNamesString;
import static org.junit.Assert.*;

import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.BriefPrintDfsVerifierListener;

/**
 * @author michael
 */
public class DfsBProgramVerifierTest {

    @Test
    public void simpleAAABTrace_forgetfulStore() throws Exception {
        BProgram program = new SingleResourceBProgram("DFSVerifierTests/AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        program.appendSource(Requirements.eventNotSelected("B"));
        sut.setVisitedNodeStore(new ForgetfulVisitedStateStore());
        VerificationResult res = sut.verify(program);
        assertTrue(res.isCounterExampleFound());
        assertEquals("AAAB", traceEventNamesString(res.getCounterExampleTrace(), ""));
    }

    @Test
    public void simpleAAABTrace() throws Exception {
        BProgram program = new SingleResourceBProgram("DFSVerifierTests/AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setDebugMode(true);

        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        program.appendSource(Requirements.eventNotSelected("B"));
        sut.setVisitedNodeStore(new BThreadSnapshotVisitedStateStore());
        VerificationResult res = sut.verify(program);
        assertTrue(res.isCounterExampleFound());
        assertEquals("AAAB", traceEventNamesString(res.getCounterExampleTrace(), ""));
    }

    @Test
    public void simpleAAABTrace_hashedNodeStore() throws Exception {
        BProgram program = new SingleResourceBProgram("DFSVerifierTests/AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setDebugMode(true);
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        program.appendSource(Requirements.eventNotSelected("B"));
        VisitedStateStore stateStore = new HashVisitedStateStore();
        sut.setVisitedNodeStore(stateStore);
        VerificationResult res = sut.verify(program);
        assertTrue(res.isCounterExampleFound());
        assertEquals("AAAB", traceEventNamesString(res.getCounterExampleTrace(), ""));
        //Add trivial getter check
        VisitedStateStore retStore = sut.getVisitedNodeStore();
        assertSame(retStore, stateStore);
    }

    @Test
    public void testAAABRun() {
        BProgram program = new SingleResourceBProgram("DFSVerifierTests/AAABTrace.js");
        BProgramRunner rnr = new BProgramRunner(program);

        rnr.addListener(new PrintBProgramRunnerListener());
        InMemoryEventLoggingListener eventLogger = rnr.addListener(new InMemoryEventLoggingListener());
        rnr.run();

        eventLogger.getEvents().forEach(System.out::println);
        assertTrue(eventNamesString(eventLogger.getEvents(), "").matches("^(AAAB)+$"));
    }

    @Test
    public void deadlockTrace() throws Exception {
        BProgram program = new SingleResourceBProgram("DFSVerifierTests/deadlocking.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setVisitedNodeStore(new ForgetfulVisitedStateStore());
        VerificationResult res = sut.verify(program);
        assertTrue(res.isCounterExampleFound());
        assertEquals(VerificationResult.ViolationType.Deadlock, res.getViolationType());
        assertEquals("A", traceEventNamesString(res.getCounterExampleTrace(), ""));
    }

    @Test
    public void testDeadlockSetting() throws Exception {
        BProgram program = new SingleResourceBProgram("DFSVerifierTests/deadlocking.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setDetectDeadlocks(false);
        VerificationResult res = sut.verify(program);
        assertFalse(res.isCounterExampleFound());
        assertEquals(VerificationResult.ViolationType.None, res.getViolationType());
    }


    @Test
    public void deadlockRun() {
        BProgram program = new SingleResourceBProgram("DFSVerifierTests/deadlocking.js");
        BProgramRunner rnr = new BProgramRunner(program);

        rnr.addListener(new PrintBProgramRunnerListener());
        InMemoryEventLoggingListener eventLogger = rnr.addListener(new InMemoryEventLoggingListener());
        rnr.run();

        eventLogger.getEvents().forEach(System.out::println);
        assertTrue(eventNamesString(eventLogger.getEvents(), "").matches("^A$"));

    }

    @Test
    public void testTwoSimpleBThreads() throws Exception {
        BProgram bprog = new StringBProgram(
                "bp.registerBThread('bt1', function(){bp.sync({ request:[ bp.Event(\"STAM1\") ] });});"
                        + "bp.registerBThread('bt2', function(){bp.sync({ request:[ bp.Event(\"STAM2\") ] });});");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        sut.setDetectDeadlocks(false);
        VerificationResult res = sut.verify(bprog);

        assertTrue(res.isVerifiedSuccessfully());
        assertEquals(3, res.getScannedStatesCount());
        assertEquals(VerificationResult.ViolationType.None, res.getViolationType());
    }

    @Test(timeout = 2000)
    public void testTwoLoopingBThreads() throws Exception {
        BProgram bprog = new StringBProgram("bp.registerBThread('bt1', function(){" + "    while(true){\n"
                + "       bp.sync({ request:[ bp.Event(\"STAM1\") ] });\n" + "}});\n"
                + "bp.registerBThread('bt2', function(){" + "    while(true){\n"
                + "       bp.sync({ request:[ bp.Event(\"STAM2\") ] });\n" + "}});\n" + "");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertFalse(res.isCounterExampleFound());
        assertEquals(1, res.getScannedStatesCount());
    }

    @Test(timeout = 2000)
    public void testVariablesInBT() throws Exception {
        BProgram bprog = new StringBProgram("bp.registerBThread('bt1', function(){" + //
                "    for(var i=0; i<10; i++){\n" + //
                "       bp.sync({ waitFor:[ bp.Event(\"X\") ] });\n" + //
                "    }\n" + //
                "    bp.sync({ block:[ bp.Event(\"X\") ] });\n" + //
                "});\n" + //

                "bp.registerBThread('bt2', function(){" + //
                "    while(true){\n" + //
                "       bp.sync({ request:[ bp.Event(\"X\") ] });\n" + //
                "}});\n" + //
                "" //
        );

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertTrue(res.isCounterExampleFound());
        assertEquals(res.getViolationType(), VerificationResult.ViolationType.Deadlock);
        assertEquals(10, res.getScannedStatesCount());
    }

    
//    @Test(timeout = 2000)
    // Ignoring until the updated Rhino is released.
    public void testVariablesEquailtyInBT() throws Exception {
        BProgram bprog = new StringBProgram( //
                "bp.registerBThread('bt1', function(){" + //
                        "    while(true) \n" + //
                        "      for(var i=0; i<10; i++){\n" + //
                        "         bp.sync({ waitFor:[ bp.Event(\"X\") ] });\n" + //
                        "      }\n" + //
                        "});\n" + "bp.registerBThread('bt2', function(){" + //
                        "    while(true){\n" + //
                        "       bp.sync({ request:[ bp.Event(\"X\") ] });\n" + //
                        "}});\n");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertFalse(res.isCounterExampleFound());
        assertEquals(res.getViolationType(), VerificationResult.ViolationType.None);
        assertEquals(10, res.getScannedStatesCount());
        assertEquals(10, res.getEdgesScanned()); //in this case only one option per state

    }

    @Test(timeout = 3000)
    public void tesJavaVariablesInBT() throws Exception {
        BProgram bprog = new StringBProgram( //
                "bp.registerBThread('bt1', function(){" + //
                        "    while(true) \n" + //
                        "      for(var i=0; i<10; i++){\n" + //
                        "         bp.sync({ request:[ bp.Event(\"X\"+i) ] });\n" + //
                        "         if (i == 5) {bp.sync({ request:[ bp.Event(\"X\"+i) ] });}\n" + //
                        "      }\n" + //
                        "});\n" +
                        "var x = bp.EventSet( \"X\", function(e){\r\n" +
                        "    return e.getName().startsWith(\"X\");\r\n" +
                        "} );\r\n" +
                        "" +
                        "bp.registerBThread('bt2', function(){" + //
                        "	 var lastE = {name:\"what\"};" + //
                        "    while(true) {\n" + //
                        "       var e = bp.sync({ waitFor: x});\n" + //
                        "		lastE = e;" + //
                        "       if( e.name == lastE.name) { bp.ASSERT(false,\"Poof\");} " + //
                        "}});\n"
        );

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new BriefPrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertEquals(res.getViolationType(), VerificationResult.ViolationType.FailedAssertion);
        assertTrue(res.isCounterExampleFound());
        assertEquals(1, res.getScannedStatesCount());
        assertEquals(1, res.getEdgesScanned());
    }

}
