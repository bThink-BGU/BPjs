package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;

import javax.swing.JFrame;

public class TicTacToeVerMain {
	
	// Add GUI for watching the model-checking run. 
	public static TTTDisplayMC TTTdisplayMC;

	public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("BPJSTicTacToe.js");

		JFrame f = new TicTacToeGameMain();
		//f.setVisible(true);
		
		TTTdisplayMC = new TTTDisplayMC(bprog); //for model checker

		BProgramRunner rnr = new BProgramRunner(bprog);
		rnr.addListener(new PrintBProgramRunnerListener() );
		rnr.start();
        
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
            System.out.printf("Scanned %,d states\n", res.getScannedStatesCount() );
            System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies() );
            
		} catch (Exception ex) {
            ex.printStackTrace(System.out);
        }


	}
	
}
