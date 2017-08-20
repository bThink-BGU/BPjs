package il.ac.bgu.cs.bp.bpjs.diningphil;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.Node;
import il.ac.bgu.cs.bp.bpjs.verification.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.verification.VerificationResult;
import java.util.Arrays;
import java.util.List;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static java.util.function.Function.identity;
import java.util.stream.Collectors;

public class DiningPhilMain {

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSDiningPhil.js");

        int philosopherCount = 5;
        if ( args.length > 1 ) {
            philosopherCount = Integer.parseInt(args[1]);
        }
        bprog.putInGlobalScope("PHILOSOPHER_COUNT", philosopherCount);
        
//        BProgramRunner rnr = new BProgramRunner(bprog);
//        rnr.addListener( new StreamLoggerListener() );
//        rnr.start();
        
		try {
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.setMaxTraceLength(50);
            final VerificationResult res = vfr.verify(bprog);
            if ( res.isCounterExampleFound() ) {
                System.out.println("Found a counterexample");
                res.getCounterExampleTrace().forEach( nd -> System.out.println(" " + nd.getLastEvent()));
                
            } else {
                System.out.println("No counterexample found.");
            }
            System.out.printf("Scanned %,d states\n", res.getStatesScanned() );
            System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies() );
            
		} catch (Exception ex) {
            ex.printStackTrace(System.out);
        }


	}
	
}
