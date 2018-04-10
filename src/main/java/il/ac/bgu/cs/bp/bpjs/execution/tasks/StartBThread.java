package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import org.mozilla.javascript.Context;

/**
 * A task to start a BThread, taking it from its entry point to its first {@code bsync}.
 */
public class StartBThread extends BPEngineTask {

    public StartBThread(BThreadSyncSnapshot aBThread, BPEngineTask.Listener l) {
        super(aBThread, l);
    }

    @Override
    BThreadSyncSnapshot callImpl( Context jsContext ) {
        jsContext.callFunctionWithContinuations(bss.getEntryPoint(), bss.getScope(), new Object[0]);
        return null;
    }
   
    @Override
    public String toString() {
        return "[StartBThread " + bss.getName() + "]";
    }
}
