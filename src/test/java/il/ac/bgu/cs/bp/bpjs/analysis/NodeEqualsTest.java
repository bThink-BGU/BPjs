package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import static org.junit.Assert.*;

import org.junit.Test;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.concurrent.ExecutorService;
import org.junit.Before;

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
        Node[] nodes = new Node[10];

        BEvent eventX = new BEvent("X");
        // Discard initial node, as it has no event, and so can't
        // be used in the even/odd equalities later.
        nodes[0] = Node.getInitialNode(bprog, exSvc).getNextNode(eventX, exSvc);

        for (int i = 1; i < 10; i++) {
            nodes[i] = nodes[i - 1].getNextNode(eventX, exSvc);
        }

        for (int i = 1; i < 10; i += 2) {
            final BThreadSyncSnapshot sysState0 = nodes[0].getSystemState().getBThreadSnapshots().iterator().next();
            final BThreadSyncSnapshot sysStatei_1 = nodes[i - 1].getSystemState().getBThreadSnapshots().iterator().next();

            assertTrue(sysState0.equals(sysStatei_1));

            assertEquals(nodes[0], nodes[i - 1]);
            assertEquals(nodes[1], nodes[i]);
        }
    }

    @Test
    public void test2() throws Exception {
        final BProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");
        bprog.putInGlobalScope("PHILOSOPHER_COUNT", 5);

        String events[] = {"Pick1R", "Pick2R", "Pick3R", "Pick4R", "Pick5R"};
        Node[] nodes = new Node[events.length + 1];

        nodes[0] = Node.getInitialNode(bprog, exSvc);

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
    }

    @Test
    public void basicsTest() throws Exception {
        // Create a program
        final BProgram bprog = new StringBProgram(P1);

        Node[] nodes = new Node[10];

        BEvent eventX = new BEvent("X");
        // Discard initial node, as it has no event, and so can't
        // be used in the even/odd equalities later.
        nodes[0] = Node.getInitialNode(bprog, exSvc).getNextNode(eventX, exSvc);

        assertTrue(nodes[0].equals(nodes[0]));
        assertFalse(nodes[0].equals(null));
        assertFalse(nodes[0].equals("Not a Node"));

    }
    
    @Before
    public void setup() {
        exSvc = ExecutorServiceMaker.makeWithName("Test");
    }
}
