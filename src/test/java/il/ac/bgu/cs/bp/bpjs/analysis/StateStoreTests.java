package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
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
        BProgram program = new ResourceBProgram("SnapshotTests/ABCDTrace.js");
        ExecutorService execSvc = BPjs.getExecutorServiceMaker().makeWithName("StoreSvc");
        BProgramSyncSnapshot bpss = program.setup().start(execSvc, program.getStorageModificationStrategy());
        DfsBProgramVerifier sut = new DfsBProgramVerifier(program);
        VisitedStateStore forgetful = new ForgetfulVisitedStateStore();
        DfsTraversalNode initial = DfsTraversalNode.getInitialNode(program, bpss);
        forgetful.store(initial.getSystemState());
        assertFalse(forgetful.isVisited(initial.getSystemState()));

        DfsTraversalNode next = sut.getUnvisitedNextNode(initial, execSvc);
        assertFalse(forgetful.isVisited(next.getSystemState()));
        
        execSvc.shutdown();
    }

    @Test
    public void VisitedStateStoreNoHashBasic() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        TestAAABTraceStore(noHash);
    }

    @Test
    public void VisitedStateStoreHashBasic() throws Exception {
        VisitedStateStore useHash = new BProgramSnapshotVisitedStateStore();
        TestAAABTraceStore(useHash);
    }

    private void TestAAABTraceStore(VisitedStateStore storeToUse) throws Exception {
        BProgram program = new ResourceBProgram("SnapshotTests/ABCDTrace.js");
        ExecutorService execSvc = BPjs.getExecutorServiceMaker().makeWithName("StoreSvc");
        BProgramSyncSnapshot bpss = program.setup().start(execSvc, program.getStorageModificationStrategy());
        DfsBProgramVerifier sut = new DfsBProgramVerifier(program);
        DfsTraversalNode initial = DfsTraversalNode.getInitialNode(program, bpss);
        storeToUse.store(initial.getSystemState());
        assertTrue(storeToUse.isVisited(initial.getSystemState()));

        DfsTraversalNode next = sut.getUnvisitedNextNode(initial, execSvc);
        assertFalse(storeToUse.isVisited(next.getSystemState()));
        storeToUse.store(next.getSystemState());
        assertTrue(storeToUse.isVisited(next.getSystemState()));
        assertTrue(storeToUse.isVisited(initial.getSystemState()));
        storeToUse.clear();
        assertFalse(storeToUse.isVisited(next.getSystemState()));
        assertFalse(storeToUse.isVisited(initial.getSystemState()));
        execSvc.shutdown();
    }

    @Test
    public void StateStoreNoHashTestDiffJSVar() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        TestDiffJSVar(noHash);
    }

    @Test
    public void StateStoreHashTestDiffJSVar() throws Exception {
        VisitedStateStore hashStore = new BProgramSnapshotVisitedStateStore();
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
        ExecutorService execSvc = BPjs.getExecutorServiceMaker().makeWithName("StoreSvcDiiffJSVar");
        BProgramSyncSnapshot bpss = program.setup().start(execSvc, program.getStorageModificationStrategy());

        DfsBProgramVerifier sut = new DfsBProgramVerifier(program);
        List<DfsTraversalNode> snapshots = new ArrayList<>();

        DfsTraversalNode initial = DfsTraversalNode.getInitialNode(program, bpss);
        storeToUse.store(initial.getSystemState());
        snapshots.add(initial);
        assertTrue(storeToUse.isVisited(initial.getSystemState()));
        DfsTraversalNode next = initial;
        //Iteration 1,starts already at request state A
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            storeToUse.store(next.getSystemState());
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next.getSystemState()));
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            storeToUse.store(next.getSystemState());
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next.getSystemState()));
        //now we want to compare specific states
        BProgramSyncSnapshot state1 = snapshots.get(1).getSystemState();
        BProgramSyncSnapshot state2 = snapshots.get(2).getSystemState();
        assertNotEquals(state1, state2);
        execSvc.shutdown();
    }

    @Test
    public void StateStoreNoHashTestEqualJSVar() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        TestEqualJSVar(noHash);
    }

    @Test
    public void StateStoreHashTestEqualJSVar() throws Exception {
        VisitedStateStore hashStore = new BProgramSnapshotVisitedStateStore();
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
        ExecutorService execSvc = BPjs.getExecutorServiceMaker().makeWithName("StoreSvcEqualJSVar");
        BProgramSyncSnapshot bpss = program.setup().start(execSvc, program.getStorageModificationStrategy());
        DfsBProgramVerifier sut = new DfsBProgramVerifier(program);
        List<DfsTraversalNode> snapshots = new ArrayList<>();

        DfsTraversalNode initial = DfsTraversalNode.getInitialNode(program, bpss);
        storeToUse.store(initial.getSystemState());
        snapshots.add(initial);
        assertTrue(storeToUse.isVisited(initial.getSystemState()));
        DfsTraversalNode next = initial;
        //Iteration 1,starts already at request state A
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            storeToUse.store(next.getSystemState());
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next.getSystemState()));
        for (int i = 0; i < 4; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            assertTrue(storeToUse.isVisited(next.getSystemState()));
            storeToUse.store(next.getSystemState());
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next.getSystemState()));
        //now we want to compare specific
        BProgramSyncSnapshot state1 = snapshots.get(1).getSystemState();
        BProgramSyncSnapshot state2 = snapshots.get(2).getSystemState();
        assertEquals(state1, state2);
        execSvc.shutdown();
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
        VisitedStateStore hashStore = new BProgramSnapshotVisitedStateStore();
        testEqualRuns(hashStore);
        assertTrue( hashStore.toString().contains( ":"+Long.toString(hashStore.getVisitedStateCount())) );
    }
    /*
        This test makes sure we compare nodes/states properly.
        these two objects (by debugging) share program counter and frame index and should differ on variables only
     */
    private void testEqualRuns(VisitedStateStore storeToUse) throws Exception {
        BProgram bprog = new ResourceBProgram("SnapshotTests/ABCDTrace.js");
        ExecutorService execSvc = BPjs.getExecutorServiceMaker().makeWithName("StoreSvcEqualJSVar");
        BProgramSyncSnapshot bpss1 = bprog.setup().start(execSvc, bprog.getStorageModificationStrategy());
        BProgramSyncSnapshot bpss2 = BProgramSyncSnapshotCloner.clone(bpss1);

        DfsBProgramVerifier sut = new DfsBProgramVerifier(bprog);

        DfsTraversalNode initial1 = DfsTraversalNode.getInitialNode(bprog, bpss1);
        DfsTraversalNode initial2 = DfsTraversalNode.getInitialNode(bprog, bpss2);

        assertEquals(initial1,initial2);

        DfsTraversalNode next1 = initial1;
        DfsTraversalNode next2 = initial2;
        storeToUse.store(next1.getSystemState());
        assertTrue(storeToUse.isVisited(next2.getSystemState()));
        for (int i = 0; i < 4; i++) {
            next1 = sut.getUnvisitedNextNode(next1, execSvc);
            storeToUse.store(next1.getSystemState());
            assertTrue(storeToUse.isVisited(next2.getSystemState()));
        }
        execSvc.shutdown();
    }

    @Test
    public void testStateStoreJavaVarsNoHash() throws Exception {
        VisitedStateStore noHash = new BThreadSnapshotVisitedStateStore();
        testStateStoreJavaVars(noHash);
    }

    @Test
    public void testStateStoreJavaVarsHash() throws Exception {
        VisitedStateStore hashStore = new BProgramSnapshotVisitedStateStore();
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
        ExecutorService execSvc = BPjs.getExecutorServiceMaker().makeWithName("StoreSvcEqualJSVar");
        BProgramSyncSnapshot bpss = bprog.setup().start(execSvc, bprog.getStorageModificationStrategy());
        DfsBProgramVerifier sut = new DfsBProgramVerifier(bprog);

        DfsTraversalNode next = DfsTraversalNode.getInitialNode(bprog, bpss);
        storeToUse.store(next.getSystemState());
        assertTrue(storeToUse.isVisited(next.getSystemState()));
        //now we enter the loop and generate initial node
        //a is 7
        next = sut.getUnvisitedNextNode(next, execSvc);
        assertFalse(storeToUse.isVisited(next.getSystemState()));
        storeToUse.store(next.getSystemState());
        //Now loop again, this should also not exist
        next = sut.getUnvisitedNextNode(next, execSvc);
        //a should be -536870912
        assertFalse(storeToUse.isVisited(next.getSystemState()));
        storeToUse.store(next.getSystemState());
        //now a should be 7 again
        //and now we should see the node
        next = sut.getUnvisitedNextNode(next, execSvc);
        assertTrue("Achiya: fails because a is 7 and not NativeJavaObject", storeToUse.isVisited(next.getSystemState()));
        execSvc.shutdown();
    }
}

