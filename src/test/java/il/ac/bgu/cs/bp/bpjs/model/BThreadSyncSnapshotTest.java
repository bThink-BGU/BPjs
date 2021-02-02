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

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import static il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy.PASSTHROUGH;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

/**
 * @author michael
 */
public class BThreadSyncSnapshotTest {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private final List<BProgramRunnerListener> listeners = new ArrayList<>();

    @Test
    public void testJSVarState() throws InterruptedException {
        BProgram bprog = new ResourceBProgram("SnapshotTests/ABCDTrace.js");
        BProgramSyncSnapshot setup = bprog.setup();

        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("BProgramSnapshotEqualityTest");
        List<BProgramSyncSnapshot> snapshots = new ArrayList<>();
        BProgramSyncSnapshot step = setup.start(execSvc, PASSTHROUGH);
        snapshots.add(step);
        //Iteration 1, starts already at request state A
        for (int i = 0; i < 4; i++) {
            Set<BEvent> possibleEvents_a = bprog.getEventSelectionStrategy().selectableEvents(step);
            EventSelectionResult event_a = bprog.getEventSelectionStrategy().select(step, possibleEvents_a).get();
            step = step.triggerEvent(event_a.getEvent(), execSvc, listeners, PASSTHROUGH);
        }
        snapshots.add(step);
        for (int i = 0; i < 4; i++) {
            Set<BEvent> possibleEvents_a = bprog.getEventSelectionStrategy().selectableEvents(step);
            EventSelectionResult event_a = bprog.getEventSelectionStrategy().select(step, possibleEvents_a).get();
            step = step.triggerEvent(event_a.getEvent(), execSvc, listeners, PASSTHROUGH);
        }
        snapshots.add(step);
        // snapshots[1] is after the first loop iteration, snapshots[2] is after the second loop iteration.
        //  They should differ in loop index.
        assertNotEquals(snapshots.get(1).getBThreadSnapshots(), snapshots.get(0).getBThreadSnapshots());
        
        execSvc.shutdown();
    }

    @Test
    public void testJavaVarState() throws InterruptedException {
        BProgram bprog = new StringBProgram("bp.registerBThread(function(){\n" +
                "        var a = new java.lang.Integer(7);\n" +
                "        while (true) {" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        a = java.lang.Integer.reverse(a);\n" +
                "        }\n" +
                "});");
        BProgramSyncSnapshot postSetup = bprog.setup();
        ExecutorService execSvcA = ExecutorServiceMaker.makeWithName("BProgramSnapshotTriggerTest");
        BProgramSyncSnapshot postSync1 = postSetup.start(execSvcA, PASSTHROUGH);
        Set<BEvent> possibleEvents = bprog.getEventSelectionStrategy().selectableEvents(postSync1);
        EventSelectionResult esr = bprog.getEventSelectionStrategy().select(postSync1, possibleEvents).get();
        BProgramSyncSnapshot postSync2 = postSync1.triggerEvent(esr.getEvent(), execSvcA, listeners, PASSTHROUGH);
        assertNotEquals(postSync1.getBThreadSnapshots(), postSync2.getBThreadSnapshots());
        execSvcA.shutdown();
    }

}

