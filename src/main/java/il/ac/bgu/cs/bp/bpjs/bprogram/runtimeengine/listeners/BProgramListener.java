package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;

/**
 * An object interested in the life-cycle of a {@link BProgram}.
 * @author michael
 */
public interface BProgramListener {
    /**
     * Called when the {@link BProgram} {@code bp} was started.
     * 
     * @param bp The BProgram started.
     */
    void started( BProgram bp );
    
    /**
     * Called when a BProgram cannot advance, and is waiting for external events 
     * to continue. For this to happen, the BProgram has to be in daemon mode.
     * 
     * @param bp The BProgram informing the change.
     * 
     * @see BProgram#setDaemonMode(boolean) 
     */
    void superstepDone( BProgram bp );
    
    /**
     * Called when the {@link BProgram} {@code bp} ends.
     * 
     * @param bp The BProgram ended.
     */
    void ended( BProgram bp );
    
    /**
     * Called when a BThread is added to a BProgram.
     * @param bp the program the thread was added to
     * @param theBThread the new BThread
     */
    void bthreadAdded( BProgram bp, BThreadSyncSnapshot theBThread );
    
    /**
     * Called when a BThread is removed from a BProgram.
     * @param bp the program the thread was removed from
     * @param theBThread the removed BThread
     */
    void bthreadRemoved( BProgram bp, BThreadSyncSnapshot theBThread );
    
    /**
     * Called when a BProgram selects an event.
     * @param bp The BProgram the event was selected in.
     * @param theEvent the new event selected.
     */
    void eventSelected( BProgram bp, BEvent theEvent );
    
}
