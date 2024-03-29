package il.ac.bgu.cs.bp.bpjs.execution.listeners;

import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.io.PrintStream;
import org.mozilla.javascript.RhinoException;

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
    public void assertionFailed(BProgram bp, il.ac.bgu.cs.bp.bpjs.model.SafetyViolationTag theFailedAssertion) {
        out.println("---:" + bp.getName() + " " + theFailedAssertion.getMessage());
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

    @Override
    public void error(BProgram bp, Exception ex) {
        out.println("/!\\ Error during run: " + ex.getMessage() );
        if ( ex instanceof BPjsRuntimeException ) {
            BPjsRuntimeException bre = (BPjsRuntimeException) ex;
            var cz = bre.getCause();
            if ( cz instanceof RhinoException ) {
                var rh = (RhinoException)cz;
                out.println( "  " + rh.details() + " at: " + rh.sourceName() + ":" + rh.lineNumber());
                StringBuilder sb = new StringBuilder();
                for ( var emt : rh.getScriptStack() ) {
                    emt.renderMozillaStyle(sb);
                }
                out.println( sb.toString() );
            }
            
        } else {
            ex.printStackTrace(out);
        }
    }
    
}
