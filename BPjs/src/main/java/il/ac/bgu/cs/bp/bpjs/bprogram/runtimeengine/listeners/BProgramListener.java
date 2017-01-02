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
     * @param bp 
     */
    void started( BProgram bp );
    
    /**
     * Called when the {@link BProgram} {@code bp} ends.
     * @param bp 
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
     * @param bp The BProgram
     * @param theEvent the new event selected.
     */
    void eventSelected( BProgram bp, BEvent theEvent );
    
    /**
     * Called when a BProgram cannot advance, and is waiting for external events 
     * to continue.
     * @param bp 
     */
    void superstepDone( BProgram bp );
    
}
