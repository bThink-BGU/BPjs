package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import org.mozilla.javascript.Context;

/**
 * A task that resumes a BThread from a BSync operation.
 */
public class ResumeBThread extends BPEngineTask {
    private final BEvent event;

    public ResumeBThread(BProgramSyncSnapshot aBpss, BThreadSyncSnapshot aBThread, BEvent selectedEvent, BPEngineTask.Listener l) {
        super(aBpss, aBThread, l);
        event = selectedEvent;
    }

    @Override
    void callImpl(Context jsContext) {        
        Object eventInJS = Context.javaToJS(event, btss.getScope());
        jsContext.resumeContinuation(btss.getContinuation(), btss.getScope(), eventInJS);
    }

    @Override
    public String toString() {
        return String.format("[ResumeBThread: %s event:%s]", btss, event);
    }
}
