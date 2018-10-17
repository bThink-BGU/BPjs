package il.ac.bgu.cs.bp.bpjs.execution.listeners;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;

/**
 * An object interested in the life-cycle of a {@link BProgram} being run by a {@link BProgramRunner}.
 * @author michael
 */
public interface BProgramRunnerListener {
    
    /**
     * Called before the BProgram is started (pre-setup).
     * 
     * @param bprog The BProgram about to start
     */
    void starting(BProgram bprog);
    
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
     * Called when a b-thread in the {@link BProgram} has made a failed assertion.
     * This means that the program is in violation of some of its requirements.
     * @param bp The program where the failed assertion happened.
     * @param theFailedAssertion Details about the assertion that failed.
     */
    void assertionFailed( BProgram bp, FailedAssertion theFailedAssertion );
    
    /**
     * Called when a BThread is added to a b-program.
     * @param bp the program the thread was added to.
     * @param theBThread the new BThread
     */
    void bthreadAdded( BProgram bp, BThreadSyncSnapshot theBThread );
    
    /**
     * Called when a BThread is removed from a b-program.
     * @param bp the program the thread was removed from.
     * @param theBThread the removed BThread
     */
    void bthreadRemoved( BProgram bp, BThreadSyncSnapshot theBThread );
    
    /**
     * Called when a BThread has ran to completion.
     * @param bp the b-program in which {@code theBThread} ran.
     * @param theBThread the done BThread
     */
    void bthreadDone( BProgram bp, BThreadSyncSnapshot theBThread);
    
    /**
     * Called when a b-program selects an event.
     * @param bp The b-program the event was selected in.
     * @param theEvent the new event selected.
     */
    void eventSelected( BProgram bp, BEvent theEvent );
    
    /**
     * Called when the b-program was halted.
     * @param bp the b-program that was halted.
     */
    void halted(BProgram bp);
}
