package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;

/**
 * A task to start a BThread, taking it from its entry point to its first {@code bsync}.
 */
public class StartBThread implements BPEngineTask {
    private final BThreadSyncSnapshot bthreadBss;

    public StartBThread(BThreadSyncSnapshot aBThread) {
        bthreadBss = aBThread;
    }

    @Override
    public BThreadSyncSnapshot call() {
         return bthreadBss.startBThread();
    }
   
    @Override
    public String toString() {
        return "[StartBThread " + bthreadBss.getName() + "]";
    }
}
