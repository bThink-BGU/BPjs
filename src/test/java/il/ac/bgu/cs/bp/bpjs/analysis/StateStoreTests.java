package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

public class StateStoreTests {

    @Test
    public void ForgetfulStore() throws Exception {
        BProgram program = new SingleResourceBProgram("SnapshotTests/ABCDTrace.js");
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("StoreSvc");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        VisitedStateStore forgetful = new ForgetfulVisitedStateStore();
        Node initial = Node.getInitialNode(program, execSvc);
        forgetful.store(initial);
        assertFalse(forgetful.isVisited(initial));

        Node next = sut.getUnvisitedNextNode(initial, execSvc);
        assertFalse(forgetful.isVisited(next));
    }

    @Test
    public void VisitedStateStoreNoHashBasic() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        TestAAABTraceStore(noHash);
    }

    @Test
    public void VisitedStateStoreHashBasic() throws Exception {
        VisitedStateStore useHash = new HashVisitedStateStore();
        TestAAABTraceStore(useHash);
    }

    private void TestAAABTraceStore(VisitedStateStore storeToUse) throws Exception {
        BProgram program = new SingleResourceBProgram("SnapshotTests/ABCDTrace.js");
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("StoreSvc");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        Node initial = Node.getInitialNode(program, execSvc);
        storeToUse.store(initial);
        assertTrue(storeToUse.isVisited(initial));

        Node next = sut.getUnvisitedNextNode(initial, execSvc);
        assertFalse(storeToUse.isVisited(next));
        storeToUse.store(next);
        assertTrue(storeToUse.isVisited(next));
        assertTrue(storeToUse.isVisited(initial));
        storeToUse.clear();
        assertFalse(storeToUse.isVisited(next));
        assertFalse(storeToUse.isVisited(initial));
    }

    @Test
    public void StateStoreNoHashTestDiffJSVar() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        TestDiffJSVar(noHash);
    }

    @Test
    public void StateStoreHashTestDiffJSVar() throws Exception {
        VisitedStateStore hashStore = new HashVisitedStateStore();
        TestDiffJSVar(hashStore);
    }

    private void TestDiffJSVar(VisitedStateStore storeToUse) throws Exception {
        BProgram program = new StringBProgram("bp.registerBThread(function(){\n" +
                "    for ( var i=0; i<4; i++ ) {\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B\")});\n" +
                "        bp.sync({request:bp.Event(\"C\")});\n" +
                "        bp.sync({request:bp.Event(\"D\")});\n" +
                "    }\n" +
                "});");
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("StoreSvcDiiffJSVar");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        List<Node> snapshots = new ArrayList<>();

        Node initial = Node.getInitialNode(program, execSvc);
        storeToUse.store(initial);
        snapshots.add(initial);
        assertTrue(storeToUse.isVisited(initial));
        Node next = initial;
        //Iteration 1,starts already at request state A
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            storeToUse.store(next);
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next));
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            storeToUse.store(next);
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next));
        //now we want to compare specific states
        BProgramSyncSnapshot state1 = snapshots.get(1).getSystemState();
        BProgramSyncSnapshot state2 = snapshots.get(2).getSystemState();
        assertNotEquals(state1, state2);
    }

    @Test
    public void StateStoreNoHashTestEqualJSVar() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        TestEqualJSVar(noHash);
    }

    @Test
    public void StateStoreHashTestEqualJSVar() throws Exception {
        VisitedStateStore hashStore = new HashVisitedStateStore();
        TestEqualJSVar(hashStore);
    }

    private void TestEqualJSVar(VisitedStateStore storeToUse) throws Exception {
        BProgram program = new StringBProgram("bp.registerBThread(function(){\n" +
                "    for ( var i=0; i<4; ) {\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        bp.sync({request:bp.Event(\"B\")});\n" +
                "        bp.sync({request:bp.Event(\"C\")});\n" +
                "        bp.sync({request:bp.Event(\"D\")});\n" +
                "    }\n" +
                "});");
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("StoreSvcEqualJSVar");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        List<Node> snapshots = new ArrayList<>();

        Node initial = Node.getInitialNode(program, execSvc);
        storeToUse.store(initial);
        snapshots.add(initial);
        assertTrue(storeToUse.isVisited(initial));
        Node next = initial;
        //Iteration 1,starts already at request state A
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            storeToUse.store(next);
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next));
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            assertTrue(storeToUse.isVisited(next));
            storeToUse.store(next);
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next));
        //now we want to compare specific
        BProgramSyncSnapshot state1 = snapshots.get(1).getSystemState();
        BProgramSyncSnapshot state2 = snapshots.get(2).getSystemState();
        assertEquals(state1, state2);
    }

    /*
        A bunch of tests from BProgramSyncSnapshot modified to check nodes.
     */


    @Test
    public void StateStoreNoHashTestIdenticalRuns() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        testEqualRuns(noHash);
    }

    @Test
    public void StateStoreHashTestIdenticalRuns() throws Exception {
        VisitedStateStore hashStore = new HashVisitedStateStore();
        testEqualRuns(hashStore);
    }
    /*
        This test makes sure we compare nodes/states properly.
        these two objects (by debugging) share program counter and frame index and should differ on variables only
     */
    private void testEqualRuns(VisitedStateStore storeToUse) throws Exception {
        BProgram bprog = new SingleResourceBProgram("SnapshotTests/ABCDTrace.js");
        BProgram bprog2 = new SingleResourceBProgram("SnapshotTests/ABCDTrace.js");


        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("StoreSvcEqualJSVar");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();

        Node initial1 = Node.getInitialNode(bprog, execSvc);
        Node initial2 = Node.getInitialNode(bprog2, execSvc);

        assertEquals(initial1,initial2);


        Node next1 = initial1;
        Node next2 = initial2;
        storeToUse.store(next1);
        assertTrue(storeToUse.isVisited(next2));
        for (int i = 0; i < 4; i++) {
            next1 = sut.getUnvisitedNextNode(next1, execSvc);
            storeToUse.store(next1);
            assertTrue(storeToUse.isVisited(next2));
        }
    }

    @Test
    public void testStateStoreJavaVarsNoHash() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        testStateStoreJavaVars(noHash);
    }

    @Test
    public void testStateStoreJavaVarsHash() throws Exception {
        VisitedStateStore hashStore = new HashVisitedStateStore();
        testStateStoreJavaVars(hashStore);
    }

    private void testStateStoreJavaVars(VisitedStateStore storeToUse) throws Exception {
        BProgram bprog = new StringBProgram("bp.registerBThread(function(){\n" +
                "        var a = new java.lang.Integer(7);\n" +
                "        bp.sync({request:bp.Event(\"A\")});\n" +
                "        while (true) {" +
                "           //bp.log.warn(a);\n" +
                "           bp.sync({request:bp.Event(\"R\")});\n" +
                "           a = java.lang.Integer.reverse(a);\n" +
                "        }\n" +
                "});");
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("StoreSvcEqualJSVar");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();

        Node next = Node.getInitialNode(bprog, execSvc);
        storeToUse.store(next);
        assertTrue(storeToUse.isVisited(next));
        //now we enter the loop and generate initial node
        //a is 7
        next = sut.getUnvisitedNextNode(next, execSvc);
        assertFalse(storeToUse.isVisited(next));
        storeToUse.store(next);
        //Now loop again, this should also not exist
        next = sut.getUnvisitedNextNode(next, execSvc);
        //a should be -536870912
        assertFalse(storeToUse.isVisited(next));
        storeToUse.store(next);
        //now a should be 7 again
        //and now we should see the node
        next = sut.getUnvisitedNextNode(next, execSvc);
        assertTrue(storeToUse.isVisited(next));

    }
}

