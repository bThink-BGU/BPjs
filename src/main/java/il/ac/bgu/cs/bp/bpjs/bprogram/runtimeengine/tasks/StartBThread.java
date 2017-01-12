package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.tasks;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;

/**
 * A task to start a BThread, taking it from its entry point to its first {@code bsync}.
 */
public class StartBThread extends BPEngineTask {
    private final BThreadSyncSnapshot bthreadBss;

    public StartBThread(BThreadSyncSnapshot aBThread) {
        bthreadBss = aBThread;
    }

    @Override
    protected BThreadSyncSnapshot run(Context jsContext) {
         try {
            jsContext.callFunctionWithContinuations(bthreadBss.getEntryPoint(), bthreadBss.getScope(), new Object[0]);
            return null;

        } catch (ContinuationPending cbs) {
            return bthreadBss.copyWith(cbs.getContinuation(), (BSyncStatement) cbs.getApplicationState());
        }
    }
   
    @Override
    public String toString() {
        return "[StartBThread " + bthreadBss.getName() + "]";
    }
}
