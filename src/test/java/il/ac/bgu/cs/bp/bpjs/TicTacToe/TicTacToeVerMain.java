package il.ac.bgu.cs.bp.bpjs.TicTacToe;

import il.ac.bgu.cs.bp.bpjs.analysis.BThreadSnapshotVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.PrioritizedBSyncEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.BriefPrintDfsVerifierListener;

/**
 * Verification of the TicTacToe strategy.
 * 
 * @author reututy
 */
public class TicTacToeVerMain  {

	public static void main(String[] args) throws InterruptedException {

		// Create a program
		BProgram bprog = new SingleResourceBProgram("BPJSTicTacToe.js");

		bprog.setEventSelectionStrategy(new PrioritizedBSyncEventSelectionStrategy());
		bprog.setWaitForExternalEvents(true);
		
		String simulatedPlayer =   "bp.registerBThread('XMoves', function() {\n" +
									"while (true) {\n" +
										"bp.sync({ request:[ X(0, 0), X(0, 1), X(0, 2), X(1, 0), \n" +
										"X(1, 1), X(1, 2), X(2, 0), X(2, 1), X(2, 2) ] }, 10); \n" +
										"}\n" +
									"});\n";
		
		// This bthread models the requirement that X never wins.
         String xCantWinRequirementBThread = "bp.registerBThread( \"req:NoXWin\", function(){\n" +
                                            "	bp.sync({waitFor:StaticEvents.XWin});\n" +
                                            "	bp.ASSERT(false, \"Found a trace where X wins.\");\n" +
                                            "});";
        
		bprog.appendSource(simulatedPlayer);
		bprog.appendSource(xCantWinRequirementBThread);
//		bprog.appendSource(infiBThread);
        try {
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.setDetectDeadlocks(false);

            vfr.setMaxTraceLength(70);
//            vfr.setDebugMode(true);
            vfr.setVisitedNodeStore(new BThreadSnapshotVisitedStateStore());
            vfr.setProgressListener( new BriefPrintDfsVerifierListener() );

            final VerificationResult res = vfr.verify(bprog);
            if (res.isCounterExampleFound()) {
                System.out.println("Found a counterexample");
                res.getCounterExampleTrace().forEach(nd -> System.out.println(" " + nd.getLastEvent()));

            } else {
                System.out.println("No counterexample found.");
            }
            System.out.printf("Scanned %,d states\n", res.getScannedStatesCount());
            System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies());

        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }

		System.out.println("end of run");
	}

}