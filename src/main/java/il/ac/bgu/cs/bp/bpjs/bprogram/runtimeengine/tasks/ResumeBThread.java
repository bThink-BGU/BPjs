package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.tasks;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;

/**
 * A task that resumes a BThread from a BSync operation.
 */
public class ResumeBThread implements BPEngineTask {
    private final BThreadSyncSnapshot bss;
    private final BEvent event;

    public ResumeBThread(BThreadSyncSnapshot aBThread, BEvent selectedEvent) {
        bss = aBThread;
        event = selectedEvent;
    }

    @Override
    public BThreadSyncSnapshot call() {
        return bss.triggerEvent(event);
    }

    @Override
    public String toString() {
        return String.format("[ResumeBThread: %s event:%s]", bss, event);
    }
}
