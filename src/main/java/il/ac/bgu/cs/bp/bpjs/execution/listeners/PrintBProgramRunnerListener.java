package il.ac.bgu.cs.bp.bpjs.execution.listeners;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import java.io.PrintStream;

/**
 * Listens to a BProgram, sends the events to an output stream.
 * Defaults to {@code System.out}.
 * @author michael
 */
public class PrintBProgramRunnerListener implements BProgramRunnerListener {
    
    private final PrintStream out;
    
    public PrintBProgramRunnerListener( PrintStream aStream ){
        out = aStream;
    }
    
    public PrintBProgramRunnerListener() {
        this( System.out );
    }

    @Override
    public void starting(BProgram bp) {
        out.println("---:" + bp.getName() + " Starting");
    }

    @Override
    public void started(BProgram bp) {
        out.println("---:" + bp.getName() + " Started");
    }

    @Override
    public void ended(BProgram bp) {
        out.println("---:" + bp.getName() + " Ended");
    }

    @Override
    public void halted(BProgram bp) {
        out.println("---:" + bp.getName() + " Halted");
    }

    @Override
    public void assertionFailed(BProgram bp, FailedAssertion theFailedAssertion) {
        out.println("---:" + bp.getName() + " B-thread " + theFailedAssertion.getBThreadName()
                           + " is in invalid state: " + theFailedAssertion.getMessage());
    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        out.println(" --:" + bp.getName() + " Event " + theEvent.toString());
    }

    @Override
    public void superstepDone(BProgram bp) {
        out.println("---:" + bp.getName() + " No Event Selected");
    }

    @Override
    public void bthreadAdded(BProgram bp, BThreadSyncSnapshot theBThread) {
        out.println("  -:" + bp.getName() + " Added " + theBThread.getName());
    }

    @Override
    public void bthreadRemoved(BProgram bp, BThreadSyncSnapshot theBThread) {
        out.println("  -:" + bp.getName() + " Removed " + theBThread.getName());
    }
    
    @Override
    public void bthreadDone(BProgram bp, BThreadSyncSnapshot theBThread) {
        out.println("  -:" + bp.getName() + " Done " + theBThread.getName());
    }
    
}
