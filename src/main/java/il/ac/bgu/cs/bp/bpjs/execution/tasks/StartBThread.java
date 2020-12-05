package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import org.mozilla.javascript.Context;

/**
 * A task to start a BThread, taking it from its entry point to its first {@code bsync}.
 */
public class StartBThread extends BPEngineTask {

    public StartBThread(BProgramSyncSnapshot aBpss, BThreadSyncSnapshot aBThread, BPEngineTask.Listener l) {
        super(aBpss, aBThread, l);
    }

    @Override
    void callImpl( Context jsContext ) {
        jsContext.callFunctionWithContinuations(btss.getEntryPoint(), btss.getEntryPoint(), new Object[0]);
    }
   
    @Override
    public String toString() {
        return "[StartBThread " + btss.getName() + "]";
    }
}
