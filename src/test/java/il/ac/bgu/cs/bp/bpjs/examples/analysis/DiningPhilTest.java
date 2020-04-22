package il.ac.bgu.cs.bp.bpjs.examples.analysis;

import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
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
        if ( res.isViolationFound()) {
            printCounterExample(res);

        } else {
            System.out.println("No counterexample found.");
            fail("No counterexample found for dinning philosophers.");
        }
    }

    private static void printCounterExample(VerificationResult res) {
        System.out.println("Found a counterexample");
        final ExecutionTrace trace = res.getViolation().get().getCounterExampleTrace();
        trace.getNodes().forEach(nd -> System.out.println(" " + nd.getEvent()));
        
        BProgramSyncSnapshot last = trace.getLastState();
        System.out.println("selectableEvents: " + trace.getBProgram().getEventSelectionStrategy()
                    .selectableEvents(last));
        last.getBThreadSnapshots().stream()
                .sorted((s1, s2) -> s1.getName().compareTo(s2.getName()))
                .forEach(s -> {
                    System.out.println(s.getName());
                    System.out.println(s.getSyncStatement());
                    System.out.println();
                });
    }

    private static VerificationResult verifyPhilosophers(int philosopherCount) throws InterruptedException {
        // Create a program
        final ResourceBProgram bprog = new ResourceBProgram("BPJSDiningPhil.js");
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
