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
package il.ac.bgu.cs.bp.bpjs.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.junit.Assert.*;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import static il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy.PASSTHROUGH;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import static java.util.Collections.emptyMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author michael
 */
public class BProgramSyncSnapshotTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private final List<BProgramRunnerListener> listeners = Collections.emptyList();
    
    public BProgramSyncSnapshotTest() {
    }

    @Test
    public void testEqualsSanity() {
        BProgram bp = new StringBProgram("");
        BProgramSyncSnapshot bss = new BProgramSyncSnapshot(bp, emptySet(), emptyMap(), emptyList(), null);
        assertEquals(bss, bss);
        Assert.assertNotEquals(bss, null);
        Assert.assertNotEquals(bss, "I'm not even the same class");
    }

    @Test
    public void testDoubleTriggerEvent() throws InterruptedException {
        BProgram bprog = new StringBProgram("bp.registerBThread(function(){\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B\")});\n" +
                "        bp.ASSERT(false,\"Failed Assert\");\n" +
                "});");
        BProgramSyncSnapshot setup = bprog.setup();
        ExecutorService execSvcA = BPjs.getExecutorServiceMaker().makeWithName("BProgramSnapshotTriggerTest");
        BProgramSyncSnapshot stepa = setup.start(execSvcA, PASSTHROUGH);
        Set<BEvent> possibleEvents_a = bprog.getEventSelectionStrategy().selectableEvents(stepa);
        EventSelectionResult event_a = bprog.getEventSelectionStrategy().select(stepa, possibleEvents_a).get();
        stepa.triggerEvent(event_a.getEvent(), execSvcA, listeners, PASSTHROUGH);
        exception.expect(IllegalStateException.class);
        stepa.triggerEvent(event_a.getEvent(), execSvcA, listeners, PASSTHROUGH);
        execSvcA.shutdown();
    }
   
    @Test
    public void testHotnessDetection() throws InterruptedException {
        BProgram bprog = new StringBProgram("bp.registerBThread(function(){\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        bp.hot(true).sync({request:bp.Event(\"B\")});\n" +
                "});");
        BProgramSyncSnapshot setup = bprog.setup();
        ExecutorService execSvcA = BPjs.getExecutorServiceMaker().makeWithName("BProgramSnapshotTriggerTest");
        BProgramSyncSnapshot bpss = setup.start(execSvcA, PASSTHROUGH);
        assertFalse(bpss.isHot());
        Set<BEvent> possibleEvents_a = bprog.getEventSelectionStrategy().selectableEvents(bpss);
        EventSelectionResult event_a = bprog.getEventSelectionStrategy().select(bpss, possibleEvents_a).get();
        bpss = bpss.triggerEvent(event_a.getEvent(), execSvcA, listeners, PASSTHROUGH);
        assertTrue(bpss.isHot());
        execSvcA.shutdown();
    }

    /*
    Test for equivalent BProgram snapshots with no variables
    This test checks for identical state if
        zero steps have run
        1 step has run
        n steps have run
        program finished
     */
    @Test
    public void testEqualsSingleStep() throws InterruptedException {
        final String sourceCode = "bp.registerBThread(function(){\n" +
            "        bp.sync({request:bp.Event(\"A\")});\n" +
            "        bp.sync({request:bp.Event(\"B\")});\n" +
            "        bp.ASSERT(false,\"Failed Assert\");\n" +
            "});";
        BProgram bprog = new StringBProgram(sourceCode);
        BProgram bprog2 = new StringBProgram(sourceCode);

        BProgramSyncSnapshot setupA = bprog.setup();
        BProgramSyncSnapshot setupB = bprog2.setup();
        assertEquals( setupA, setupB );
        
        
        // Run first step
        ExecutorService execSvcA = BPjs.getExecutorServiceMaker().makeWithName("BProgramSnapshotEqualityTest");
        ExecutorService execSvcB = BPjs.getExecutorServiceMaker().makeWithName("BProgramSnapshotEqualityTest");
        BProgramSyncSnapshot step1A = setupA.start(execSvcA, PASSTHROUGH);
        BProgramSyncSnapshot step1B = setupB.start(execSvcB, PASSTHROUGH);
        assertEquals(step1A, step1B);
        assertNotEquals(setupA, step1A);
        assertNotEquals(setupB, step1B);

        //run second step
        Set<BEvent> possibleEventsA = bprog.getEventSelectionStrategy().selectableEvents(step1A);
        Set<BEvent> possibleEventsB = bprog2.getEventSelectionStrategy().selectableEvents(step1B);
        EventSelectionResult eventA = bprog.getEventSelectionStrategy().select(step1A, possibleEventsA).get();
        EventSelectionResult eventB = bprog2.getEventSelectionStrategy().select(step1A, possibleEventsB).get();
        BProgramSyncSnapshot step2A = step1A.triggerEvent(eventA.getEvent(), execSvcA, listeners, PASSTHROUGH);
        BProgramSyncSnapshot step2B = step1B.triggerEvent(eventB.getEvent(), execSvcB, listeners, PASSTHROUGH);
        assertEquals(step2A, step2B);
        assertNotEquals(step1A, step2A);
        assertNotEquals(step1B, step2B);

        possibleEventsA = bprog.getEventSelectionStrategy().selectableEvents(step2A);
        possibleEventsB = bprog2.getEventSelectionStrategy().selectableEvents(step2B);
        eventA = bprog.getEventSelectionStrategy().select(step2A, possibleEventsA).get();
        eventB = bprog2.getEventSelectionStrategy().select(step2B, possibleEventsB).get();
        BProgramSyncSnapshot step3A = step2A.triggerEvent(eventA.getEvent(), execSvcA, listeners, PASSTHROUGH);
        BProgramSyncSnapshot step3B = step2B.triggerEvent(eventB.getEvent(), execSvcB, listeners, PASSTHROUGH);
        assertEquals(step3A, step3B);
        assertNotEquals(step3A, step2A);
        assertNotEquals(step3B, step2A);
        assertEquals(step3A, step3B);
        assertNotEquals(step3A, step2A);
        assertNotEquals(step3B, step2A);
        assertTrue(step3A.noBThreadsLeft());
        execSvcA.shutdown();
        execSvcB.shutdown();
    }


    /**
     *  Test for equivalent BProgram with different assertions
     */
    @Test
    public void testEqualsSingleStepAssert() throws InterruptedException {
        BProgram bprog1 = new StringBProgram("bp.registerBThread(function(){\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B\")});\n" +
                "        bp.ASSERT(false,\"Failed Assert\");\n" +
                "});");
        BProgram bprog2 = new StringBProgram("bp.registerBThread(function(){\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B\")});\n" +
                "        bp.ASSERT(false,\"Failed Assert2\");\n" +
                "});");

        BProgramSyncSnapshot setup1 = bprog1.setup();
        BProgramSyncSnapshot setup2 = bprog2.setup();

        // Run first step
        ExecutorService execSvcA = BPjs.getExecutorServiceMaker().makeWithName("BProgramSnapshotEqualityTest");
        ExecutorService execSvcB = BPjs.getExecutorServiceMaker().makeWithName("BProgramSnapshotEqualityTest");
        BProgramSyncSnapshot postStart1 = setup1.start(execSvcA, PASSTHROUGH);
        BProgramSyncSnapshot postStart2 = setup2.start(execSvcB, PASSTHROUGH);
        assertNotEquals("The source code of the two bthreads is different, thus they should not be equal.", postStart1, postStart2);
        assertNotEquals(setup1, postStart1);
        assertNotEquals(setup2, postStart2);
        
        // Run second step
        Set<BEvent> possibleEvents1 = bprog1.getEventSelectionStrategy().selectableEvents(postStart1);
        Set<BEvent> possibleEvents2 = bprog2.getEventSelectionStrategy().selectableEvents(postStart2);
        EventSelectionResult event1_1 = bprog1.getEventSelectionStrategy().select(postStart1, possibleEvents1).get();
        EventSelectionResult event1_2 = bprog2.getEventSelectionStrategy().select(postStart2, possibleEvents2).get();
        assertEquals( event1_1, event1_2 );


        execSvcA.shutdown();
        execSvcB.shutdown();
    }
}
