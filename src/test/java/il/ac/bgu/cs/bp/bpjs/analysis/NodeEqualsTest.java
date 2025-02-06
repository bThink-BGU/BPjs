package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import static org.junit.Assert.*;

import il.ac.bgu.cs.bp.bpjs.model.*;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import org.junit.After;
import org.junit.Before;
import org.mozilla.javascript.EcmaError;

public class NodeEqualsTest {

    static final String P1
            = "bp.registerBThread(\"BThread 1\", function() {"
            + "	while (true) {"
            + "		bp.sync({request: bp.Event(\"X\")});"
            + "		bp.sync({wait : bp.Event(\"X\")});"
            + "	}"
            + "});";
    
    ExecutorService exSvc;

    @Test
    public void test1() throws Exception {
        // Create a program
        final BProgram bprog = new StringBProgram(P1);
        BProgramSyncSnapshot bpss = bprog.setup().start(exSvc, bprog.getStorageModificationStrategy());
        DfsTraversalNode[] nodes = new DfsTraversalNode[10];

        BEvent eventX = new BEvent("X");
        // Discard initial node, as it has no event, and so can't
        // be used in the even/odd equalities later.
        nodes[0] = DfsTraversalNode.getInitialNode(bprog, bpss).getNextNode(eventX, exSvc);

        for (int i = 1; i < 10; i++) {
            nodes[i] = nodes[i - 1].getNextNode(eventX, exSvc);
        }

        for (int i = 1; i < 10; i += 2) {
            final BThreadSyncSnapshot sysState0 = nodes[0].getSystemState().getBThreadSnapshots().iterator().next();
            final BThreadSyncSnapshot sysStatei_1 = nodes[i - 1].getSystemState().getBThreadSnapshots().iterator().next();

            assertTrue("Failed equality for node num " + i, sysState0.equals(sysStatei_1));

            assertEquals("Failed equality for node num " + (i-1), nodes[0], nodes[i - 1]);
            assertEquals("Failed equality for node num " + i, nodes[1], nodes[i]);
        }
    }

    @Test
    public void test2() throws Exception {
        try {
            final BProgram bprog = new ResourceBProgram("BPJSDiningPhil.js");
            bprog.putInGlobalScope("PHILOSOPHER_COUNT", 5);
            BProgramSyncSnapshot bpss = bprog.setup().start(exSvc, bprog.getStorageModificationStrategy());

            String events[] = {"Pick1R", "Pick2R", "Pick3R", "Pick4R", "Pick5R"};
            DfsTraversalNode[] nodes = new DfsTraversalNode[events.length + 1];
            nodes[0] = DfsTraversalNode.getInitialNode(bprog, bpss);

            for (int i = 0; i < events.length; i++) {
                nodes[i + 1] = nodes[i].getNextNode(new BEvent(events[i]), exSvc);
            }

            for (int i = 0; i < nodes.length; i++) {
                for (int j = 0; j < nodes.length; j++) {
                    if (i != j) {
                        assertFalse("node " + i + " should not equal node " + j, nodes[i].equals(nodes[j]));
                    }
                }
            }
        } catch ( EcmaError e ) {
            System.out.println("EcmaError during test");
            System.out.println(e.details());
            System.out.println(e.getErrorMessage());
            System.out.println(e.getName());
            System.out.println(e.sourceName() + ":" + e.lineNumber() + " " + e.lineSource());
            System.out.println(e.getScriptStackTrace());
            throw e;
        }
    }

    @Test
    public void basicsTest() throws Exception {
        // Create a program
        final BProgram bprog = new StringBProgram(P1);
        BProgramSyncSnapshot bpss = bprog.setup().start(exSvc, bprog.getStorageModificationStrategy());

        DfsTraversalNode[] nodes = new DfsTraversalNode[10];

        BEvent eventX = new BEvent("X");
        // Discard initial node, as it has no event, and so can't
        // be used in the even/odd equalities later.
        nodes[0] = DfsTraversalNode.getInitialNode(bprog, bpss).getNextNode(eventX, exSvc);

        assertTrue(nodes[0].equals(nodes[0]));
        assertFalse(nodes[0].equals(null));
        assertFalse(nodes[0].equals("Not a Node"));

    }
    
    @Before
    public void setup() {
        exSvc = BPjs.getExecutorServiceMaker().makeWithName("Test");
    }
    
    @After
    public void teardown() {
        exSvc.shutdown();
    }
}
