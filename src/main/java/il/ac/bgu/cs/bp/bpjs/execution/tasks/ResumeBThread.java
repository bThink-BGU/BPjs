package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import org.mozilla.javascript.Context;

/**
 * A task that resumes a BThread from a BSync operation.
 */
public class ResumeBThread extends BPEngineTask {
    private final BEvent event;

    public ResumeBThread(BThreadSyncSnapshot aBThread, BEvent selectedEvent, BPEngineTask.Listener l) {
        super(aBThread, l);
        event = selectedEvent;
    }

    @Override
    BThreadSyncSnapshot callImpl(Context jsContext) {        
        Object eventInJS = Context.javaToJS(event, bss.getScope());
        jsContext.resumeContinuation(bss.getContinuation(), bss.getScope(), eventInJS);
        return null;
    }

    @Override
    public String toString() {
        return String.format("[ResumeBThread: %s event:%s]", bss, event);
    }
}
