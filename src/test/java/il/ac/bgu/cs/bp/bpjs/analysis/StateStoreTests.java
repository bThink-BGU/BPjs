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
        VisitedStateStore noHash = new BProgramStateVisitedStateStore(false);
        TestAAABTraceStore(noHash);
    }

    @Test
    public void VisitedStateStoreHashBasic() throws Exception {
        VisitedStateStore useHash = new BProgramStateVisitedStateStore(true);
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
        VisitedStateStore noHash = new BProgramStateVisitedStateStore(false);
        TestDiffJSVar(noHash);
    }

    @Test
    public void StateStoreHashTestDiffJSVar() throws Exception {
        VisitedStateStore hashStore = new BProgramStateVisitedStateStore(true);
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
        VisitedStateStore noHash = new BProgramStateVisitedStateStore(false);
        TestEqualJSVar(noHash);
    }

    @Test
    public void StateStoreHashTestEqualJSVar() throws Exception {
        VisitedStateStore hashStore = new BProgramStateVisitedStateStore(true);
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
            storeToUse.store(next);
        }
        snapshots.add(next);
        assertTrue(storeToUse.isVisited(next));
        //now we want to compare specific
        BProgramSyncSnapshot state1 = snapshots.get(1).getSystemState();
        BProgramSyncSnapshot state2 = snapshots.get(2).getSystemState();
        assertEquals(state1, state2);
    }

}

