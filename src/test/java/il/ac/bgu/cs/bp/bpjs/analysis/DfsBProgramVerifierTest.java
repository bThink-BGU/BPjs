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

import il.ac.bgu.cs.bp.bpjs.TestUtils;
import static il.ac.bgu.cs.bp.bpjs.TestUtils.eventNamesString;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;

import org.junit.Test;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.traceEventNamesString;
import static org.junit.Assert.*;

import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.DeadlockViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.DetectedSafetyViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.JsErrorViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static java.util.stream.Collectors.joining;

/**
 * @author michael
 */
public class DfsBProgramVerifierTest {
    
    @Test
    public void sanity() throws Exception {
        BProgram program = new ResourceBProgram("DFSVerifierTests/AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setDebugMode(true);
        sut.setMaxTraceLength(3);
        sut.setIterationCountGap(1);
        sut.verify(program);
        assertEquals( ExecutionTraceInspections.DEFAULT_SET, sut.getInspections() );
    }

    @Test
    public void simpleAAABTrace_forgetfulStore() throws Exception {
        BProgram program = new ResourceBProgram("DFSVerifierTests/AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setProgressListener(new PrintDfsVerifierListener());
        program.appendSource(Requirements.eventNotSelected("B"));
        sut.setVisitedStateStore(new ForgetfulVisitedStateStore());
        VerificationResult res = sut.verify(program);
        assertTrue(res.isViolationFound());
        assertEquals("AAAB", traceEventNamesString(res, ""));
    }

    @Test
    public void simpleAAABTrace() throws Exception {
        BProgram program = new ResourceBProgram("DFSVerifierTests/AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setDebugMode(true);

        sut.setProgressListener(new PrintDfsVerifierListener());
        program.appendSource(Requirements.eventNotSelected("B"));
        sut.setVisitedStateStore(new BThreadSnapshotVisitedStateStore());
        VerificationResult res = sut.verify(program);
        assertTrue(res.isViolationFound());
        assertEquals("AAAB", traceEventNamesString(res, ""));
    }

    @Test
    public void simpleAAABTrace_hashedNodeStore() throws Exception {
        BProgram program = new ResourceBProgram("DFSVerifierTests/AAABTrace.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setDebugMode(true);
        sut.setProgressListener(new PrintDfsVerifierListener());
        program.appendSource(Requirements.eventNotSelected("B"));
        VisitedStateStore stateStore = new BProgramSnapshotVisitedStateStore();
        sut.setVisitedStateStore(stateStore);
        VerificationResult res = sut.verify(program);
        assertTrue(res.isViolationFound());
        assertEquals("AAAB", traceEventNamesString(res, ""));
        //Add trivial getter check
        VisitedStateStore retStore = sut.getVisitedStateStore();
        assertSame(retStore, stateStore);
    }

    @Test
    public void testAAABRun() {
        BProgram program = new ResourceBProgram("DFSVerifierTests/AAABTrace.js");
        BProgramRunner rnr = new BProgramRunner(program);

        rnr.addListener(new PrintBProgramRunnerListener());
        InMemoryEventLoggingListener eventLogger = rnr.addListener(new InMemoryEventLoggingListener());
        rnr.run();

        eventLogger.getEvents().forEach(System.out::println);
        assertTrue(eventNamesString(eventLogger.getEvents(), "").matches("^(AAAB)+$"));
    }

    @Test
    public void deadlockTrace() throws Exception {
        BProgram program = new ResourceBProgram("DFSVerifierTests/deadlocking.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setVisitedStateStore(new ForgetfulVisitedStateStore());
        VerificationResult res = sut.verify(program);
        
        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DeadlockViolation);
        assertEquals("A", traceEventNamesString(res, ""));
    }

    @Test
    public void testDeadlockSetting() throws Exception {
        BProgram program = new ResourceBProgram("DFSVerifierTests/deadlocking.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);
        VerificationResult res = sut.verify(program);
        assertFalse(res.isViolationFound());
    }


    @Test
    public void deadlockRun() {
        BProgram program = new ResourceBProgram("DFSVerifierTests/deadlocking.js");
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
                "bp.registerBThread('bt1', function(){bp.sync({ request:[ bp.Event(\"STAM1\") ] });});" +
                "bp.registerBThread('bt2', function(){bp.sync({ request:[ bp.Event(\"STAM2\") ] });});");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new PrintDfsVerifierListener());
        sut.addInspection(ExecutionTraceInspections.FAILED_ASSERTIONS);
        VerificationResult res = sut.verify(bprog);

        assertFalse(res.isViolationFound());
        assertEquals(4, res.getScannedStatesCount());
        assertEquals(4, res.getScannedEdgesCount());
    }

    @Test(timeout = 2000)
    public void testTwoLoopingBThreads() throws Exception {
        BProgram bprog = new StringBProgram("bp.registerBThread('bt1', function(){" + "    while(true){\n"
                + "       bp.sync({ request:[ bp.Event(\"STAM1\") ] });\n" + "}});\n"
                + "bp.registerBThread('bt2', function(){" + "    while(true){\n"
                + "       bp.sync({ request:[ bp.Event(\"STAM2\") ] });\n" + "}});\n" + "");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new PrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertFalse(res.isViolationFound());
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
        sut.setProgressListener(new PrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DeadlockViolation);
        assertEquals(11, res.getScannedStatesCount());
    }

    
    @Test(timeout = 2000)
    public void testVariablesEquailtyInBT() throws Exception {
        BProgram bprog = new StringBProgram( //
                "bp.registerBThread('bt1', function(){" + //
                "    bp.sync({ waitFor:[ bp.Event(\"X\") ] });\n" + // 1
                "    bp.sync({ waitFor:[ bp.Event(\"X\") ] });\n" + // 2
                "    bp.sync({ waitFor:[ bp.Event(\"X\") ] });\n" + // 3
                "    bp.sync({ waitFor:[ bp.Event(\"X\") ] });\n" + // 4
                "});\n" + 
                "bp.registerBThread('bt2', function(){" + //
                "    while(true){\n" + //
                "       bp.sync({ request:[ bp.Event(\"X\") ] });\n" + //
                "}});\n");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new PrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertFalse(res.isViolationFound());
        // 10 syncs while bt1 is alive, 1 sync per bt2's infinite loop alone.
        assertEquals(5, res.getScannedStatesCount());
        
        // in this case only one option per state
        assertEquals(5, res.getScannedEdgesCount()); 

    }
    
     @Test(timeout = 2000)
    public void testMaxTraceLength() throws Exception {
        String source = "bp.registerBThread('bt1', function(){" + 
                "    bp.sync({ request:[ bp.Event(\"X\") ] });\n" + 
                "    bp.sync({ request:[ bp.Event(\"X\") ] });\n" + 
                "    bp.sync({ request:[ bp.Event(\"X\") ] });\n" + 
                "    bp.sync({ request:[ bp.Event(\"X\") ] });\n" + 
                "    bp.sync({ request:[ bp.Event(\"X\") ] });\n" + 
                "    bp.sync({ request:[ bp.Event(\"X\") ] });\n" + 
                "    bp.ASSERT(false, \"\");" +
                "});";
        BProgram bprog = new StringBProgram(source);

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new PrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);
        assertTrue(res.isViolationFound());
        
        sut.setMaxTraceLength(6);
        res = sut.verify(new StringBProgram(source));
        assertFalse(res.isViolationFound());

    }

    @Test(timeout = 6000)
    public void testJavaVariablesInBT() throws Exception {
        BProgram bprog = new StringBProgram( //
                "bp.registerBThread('bt1', function(){" + //
                        "  var sampleArray=[1,2,3,4,5];\n" +
                        "    while(true) \n" + //
                        "      for(var i=0; i<10; i++){\n" + //
                        "         bp.sync({ request:[ bp.Event(\"X\"+i) ] });\n" + //
                        "         if (i == 5) {bp.sync({ request:[ bp.Event(\"X\"+i) ] });}\n" + //
                        "      }\n" + //
                        "});\n" +
                        "var xs = bp.EventSet( \"X\", function(e){\r\n" +
                        "    return e.getName().startsWith(\"X\");\r\n" +
                        "} );\r\n" +
                        "" +
                        "bp.registerBThread('bt2', function(){" + //
                        "	 var lastE = {name:\"what\"};" + //
                        "    while(true) {\n" + //
                        "       var e = bp.sync({ waitFor: xs});\n" + //
                        "		lastE = e;" + //
                        "       if( e.name == lastE.name) { bp.ASSERT(false,\"Poof\");} " + //
                        "}});\n"
        );

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setIterationCountGap(1);
        sut.setProgressListener(new PrintDfsVerifierListener());
        sut.setDebugMode(true);
        VerificationResult res = sut.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DetectedSafetyViolation);
        assertEquals(2, res.getScannedStatesCount());
        assertEquals(1, res.getScannedEdgesCount());
    }
    
    
    /**
     * Running this transition system. State 3 should be arrived at twice.
     *           +-»B1»--+
     *           |       |
     * -»1--»A»--2       3--»C»----+
     *   |       |       |         |
     *   |       +-»B2»--+         |
     *   +------------«------------+       
     * 
     * event trace "AB1-" is the result of execution
     * 
     * -»1-»A»-2-»B1»-3
     * 
     * Whose stack is:
     * 
     * +---+----+---+
     * |1,A|2,B1|3,-|
     * +---+----+---+
     * 
     * With C selected, we get to 
     * 
     * +---+----+---+
     * |1,A|2,B1|3,C| + cycleTo 0
     * +---+----+---+
     * 
     * @throws Exception 
     */
    @Test
    public void testDoublePathDiscovery() throws Exception {
        BProgram bprog = new StringBProgram( //
                "bp.registerBThread(\"round\", function(){\n" +
                "    while( true ) {\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        bp.sync({waitFor:[bp.Event(\"B1\"), bp.Event(\"B2\")]});\n" +
                "        bp.sync({request:bp.Event(\"C\")});\n" +
                "    }\n" +
                "});\n" +
                "\n" +
                "bp.registerBThread(\"round-s1\", function(){\n" +
                "    while( true ) {\n" +
                "        bp.sync({waitFor:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B1\"), waitFor:bp.Event(\"B2\")});\n" +
                "    }\n" +
                "});\n" +
                "\n" +
                "bp.registerBThread(\"round-s2\", function(){\n" +
                "    while( true ) {\n" +
                "        bp.sync({waitFor:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B2\"), waitFor:bp.Event(\"B1\")});\n" +
                "    }\n" +
                "});");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        
        final Set<String> foundTraces = new HashSet<>();
        sut.addInspection( ExecutionTraceInspection.named("DFS trace captures", et->{
            String eventTrace = et.getNodes().stream()
                .map( n->n.getEvent() )
                .map( o->o.map(BEvent::getName).orElse("-") )
                .collect( joining("") );
            System.out.println("eventTrace = " + eventTrace);
            foundTraces.add(eventTrace);
            return Optional.empty();
        }));
        
        sut.setProgressListener(new PrintDfsVerifierListener());
        VerificationResult res = sut.verify(bprog);

        assertEquals(3, res.getScannedStatesCount());
        assertEquals(4, res.getScannedEdgesCount());
        
        Set<String> expected1 = new TreeSet<>(Arrays.asList("-","A-","AB1-","AB1C", "AB2-"));
        Set<String> expected2 = new TreeSet<>(Arrays.asList("-","A-","AB2-","AB2C", "AB1-"));
        String eventTraces = foundTraces.stream().sorted().collect( joining(", ") );
        
        assertTrue("Traces don't match expected: " + eventTraces,
                    foundTraces.equals(expected1) || foundTraces.equals(expected2) );
        
        System.out.println("Event traces: " + eventTraces);
    }

    /**
     * Program graph is same as above, but state "3" is duplicated since a b-thread
     * holds the last event that happened in a variable.
     *
     *   +------------«------------+        
     *   |                         | 
     *   v       +-»B1»--3'---»C»--+
     *   |       |       
     * -»1--»A»--2       
     *   |       |                
     *   ^       +-»B2»--3''--»C»--+
     *   |                         |
     *   +------------«------------+       
     * 
     * 
     * 
     * @throws Exception 
     */
    @Test
    public void testDoublePathWithVariablesDiscovery() throws Exception {
        BProgram bprog = new StringBProgram("doubleWithVar", //
                "bp.registerBThread(\"round\", function(){\n" +
                "    var le=null;\n" +
                "    while( true ) {\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        le = bp.sync({waitFor:[bp.Event(\"B1\"), bp.Event(\"B2\")]});\n" +
                "        bp.sync({request:bp.Event(\"C\")});\n" +
                "        le=null;\n " +
                "    }\n" +
                "});\n" +
                "\n" +
                "bp.registerBThread(\"round-s1\", function(){\n" +
                "    while( true ) {\n" +
                "        bp.sync({waitFor:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B1\"), waitFor:bp.Event(\"B2\")});\n" +
                "    }\n" +
                "});\n" +
                "\n" +
                "bp.registerBThread(\"round-s2\", function(){\n" +
                "    while( true ) {\n" +
                "        bp.sync({waitFor:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B2\"), waitFor:bp.Event(\"B1\")});\n" +
                "    }\n" +
                "});");

        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        
        final Set<String> foundTraces = new HashSet<>();
        sut.addInspection( ExecutionTraceInspection.named("DFS trace captures", et->{
            String eventTrace = et.getNodes().stream()
                .map( n->n.getEvent() )
                .map( o->o.map(BEvent::getName).orElse("-") )
                .collect( joining("") );
            System.out.println("eventTrace = " + eventTrace);
            foundTraces.add(eventTrace);
            return Optional.empty();
        }));
        sut.setVisitedStateStore( new BProgramSnapshotVisitedStateStore() );
        sut.setProgressListener(new PrintDfsVerifierListener());
        VerificationResult res = sut.verify(bprog);

        assertEquals(4, res.getScannedStatesCount());
        assertEquals(5, res.getScannedEdgesCount());
        
        Set<String> expectedTraces = new TreeSet<>(Arrays.asList("-","A-","AB1-","AB1C","AB2-","AB2C"));
        
        assertEquals("Traces don't match expected: " + foundTraces, expectedTraces, foundTraces );
        
    }
    
    @Test
    public void testJavaScriptError() throws Exception {
        BProgram bprog = new StringBProgram(
              "bp.registerBThread( function(){\n"
            + "  bp.sync({request:bp.Event(\"A\")});\n"
            + "  bp.sync({request:bp.Event(\"A\")});\n"
            + "  bp.sync({request:bp.Event(\"A\")});\n"
            + "  var myNullVar;\n"
            + "  myNullVar.isNullAndSoThisInvocationShouldCrash();\n"
            + "  bp.sync({request:bp.Event(\"A\")});\n"
            + "});"
        );
        
        final AtomicBoolean errorCalled = new AtomicBoolean();
        final AtomicBoolean errorMadeSense = new AtomicBoolean();
        
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setProgressListener(new DfsBProgramVerifier.ProgressListener() {
            @Override public void started(DfsBProgramVerifier vfr) {}
            @Override public void iterationCount(long count, long statesHit, DfsBProgramVerifier vfr) {}
            @Override public void maxTraceLengthHit(ExecutionTrace aTrace, DfsBProgramVerifier vfr) {}
            @Override public void done(DfsBProgramVerifier vfr) {}

            @Override
            public boolean violationFound(Violation aViolation, DfsBProgramVerifier vfr) {
                errorCalled.set(aViolation instanceof JsErrorViolation );
                JsErrorViolation jsev = (JsErrorViolation) aViolation;
                errorMadeSense.set(jsev.decsribe().contains("isNullAndSoThisInvocationShouldCrash"));
                System.out.println(jsev.getThrownException().getMessage());
                return true;
            }
        });
        
        sut.verify( bprog );
        
        assertTrue( errorCalled.get() );
        assertTrue( errorMadeSense.get() );
    }
    
    /**
     * Test that even with forgetful storage, a circular trace does not get 
     * use to an infinite run.
     * @throws java.lang.Exception
     */
    @Test//(timeout=6000)
    public void testCircularTraceDetection_forgetfulStorage() throws Exception {
        String bprog = "bp.registerBThread(function(){\n"
            + "while (true) {\n"
            + "  bp.sync({request:bp.Event(\"X\")});"
            + "  bp.sync({request:bp.Event(\"Y\")});"
            + "  bp.sync({request:bp.Event(\"Z\")});"
            + "  bp.sync({request:bp.Event(\"W\")});"
            + "  bp.sync({request:bp.Event(\"A\")});"
            + "  bp.sync({request:bp.Event(\"B\")});"
            + "}"
            + "});";
        
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setVisitedStateStore( new ForgetfulVisitedStateStore() );
        final AtomicInteger cycleToIndex = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicReference<String> lastEventName = new AtomicReference<>();
        sut.addInspection(ExecutionTraceInspection.named("Cycle", t->{
            if ( t.isCyclic() ) {
                cycleToIndex.set( t.getCycleToIndex() );
                lastEventName.set( t.getLastEvent().get().getName() );
                System.out.println(TestUtils.traceEventNamesString(t, ", "));
            }
            return Optional.empty();
        }));
        
        VerificationResult res = sut.verify(new StringBProgram(bprog));
        System.out.println("states: " + res.getScannedStatesCount());
        assertEquals( 0, cycleToIndex.get() );
        assertEquals( "B", lastEventName.get() );
        
    }
    
    @Test
    public void testThreadStorageEquality() throws Exception {
        BProgram program = new ResourceBProgram("hotColdThreadMonitor.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setProgressListener(new PrintDfsVerifierListener());
        VisitedStateStore stateStore = new BThreadSnapshotVisitedStateStore();
        sut.setVisitedStateStore(stateStore);
        VerificationResult res = sut.verify(program);
        
        assertEquals( 9, res.getScannedStatesCount() );
        assertEquals( 9, stateStore.getVisitedStateCount() );
    }
}
