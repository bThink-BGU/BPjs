package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.io.PrintStream;

/**
 * Listens to a BProgram, sends the events to an output stream.
 * Defaults to {@code System.out}.
 * @author michael
 */
public class PrintBProgramListener implements BProgramListener {
    
    private final PrintStream out;
    
    public PrintBProgramListener( PrintStream aStream ){
        out = aStream;
    }
    
    public PrintBProgramListener() {
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
