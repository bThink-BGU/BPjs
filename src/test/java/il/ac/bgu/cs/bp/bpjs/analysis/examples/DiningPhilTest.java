package il.ac.bgu.cs.bp.bpjs.analysis.examples;

import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.analysis.Node;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import java.util.List;
import static org.junit.Assert.fail;
import org.junit.Test;

public class DiningPhilTest {
    
    public static void main(String[] args) throws InterruptedException {
        // testing the dining philosophers for a large number of philosophers
        printCounterExample(verifyPhilosophers(15));
    }

    @Test
    public void testCounterexampleFound() throws InterruptedException {
        VerificationResult res = verifyPhilosophers(5);
        if ( res.isCounterExampleFound() ) {
            printCounterExample(res);

        } else {
            System.out.println("No counterexample found.");
            fail("No counterexample found for dinning philosophers.");
        }
    }

    private static void printCounterExample(VerificationResult res) {
        System.out.println("Found a counterexample");
        final List<Node> trace = res.getCounterExampleTrace();
        trace.forEach(nd -> System.out.println(" " + nd.getLastEvent()));
        
        Node last = trace.get(trace.size() - 1);
        System.out.println("selectableEvents: " + last.getSelectableEvents());
        last.getSystemState().getBThreadSnapshots().stream()
                .sorted((s1, s2) -> s1.getName().compareTo(s2.getName()))
                .forEach(s -> {
                    System.out.println(s.getName());
                    System.out.println(s.getBSyncStatement());
                    System.out.println();
                });
    }

    private static VerificationResult verifyPhilosophers(int philosopherCount) throws InterruptedException {
        // Create a program
        final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");
        bprog.putInGlobalScope("PHILOSOPHER_COUNT", philosopherCount);

        try {
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.setMaxTraceLength(50);
            final VerificationResult res = vfr.verify(bprog);

            System.out.printf("Scanned %,d states\n", res.getScannedStatesCount());
            System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies());

            return res;

        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

}
