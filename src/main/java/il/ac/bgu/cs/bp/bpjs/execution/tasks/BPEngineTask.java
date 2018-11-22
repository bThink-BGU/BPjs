package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.analysis.bprogramio.BThreadSyncSnapshotOutputStream;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import java.util.concurrent.Callable;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import il.ac.bgu.cs.bp.bpjs.model.ForkStatement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;

/**
 * Base interface for a parallel task executed during the execution of a {@link BProgram}.

* @author Michael
 */
public abstract class BPEngineTask implements Callable<BThreadSyncSnapshot>{    
    
    /**
     * Callback interface for when assertions fail.
     */
    public static interface Listener {
        public void assertionFailed( FailedAssertion fa );
        public void addFork( ForkStatement stmt );
    }
    
    protected final BThreadSyncSnapshot bss;
    protected final Listener listener;

    BPEngineTask(BThreadSyncSnapshot aBss, Listener aListener) {
        listener = aListener;
        bss = aBss;
    }
    
    abstract BThreadSyncSnapshot callImpl(Context jsContext);
    
    @Override
    public BThreadSyncSnapshot call() {

        Context jsContext = Context.enter();
        try {            
            return callImpl( jsContext );

        } catch (ContinuationPending cbs) {
            return handleContinuationPending(cbs, jsContext);
           
        } catch ( WrappedException wfae ) {
            return handleWrappedException(wfae);
            
        } catch ( Throwable generalThrowable ) {
            System.err.println("BPjs Error: Unhandled exception in BPEngineTask.");
            System.err.println("            This is a bug in BPjs. Please report. Sorry.");
            generalThrowable.printStackTrace( System.err );
            
            throw generalThrowable;
            
        } finally {
            if (Context.getCurrentContext() != null) {
                Context.exit();
            }
        }
        
    }
    
    /**
     * Handle a captures continuation. This can be because of a sync statement, 
     * or because of a fork.
     * @param cbs
     * @param jsContext
     * @return Snapshot for the continued execution of the parent.
     * @throws IllegalStateException 
     */
    private BThreadSyncSnapshot handleContinuationPending(ContinuationPending cbs, Context jsContext) throws IllegalStateException {
        final Object capturedStatement = cbs.getApplicationState();
        
        if ( capturedStatement instanceof BSyncStatement ) {
            final BSyncStatement syncStatement = (BSyncStatement) cbs.getApplicationState();
            syncStatement.setBthread(bss);
            return bss.copyWith(cbs.getContinuation(), syncStatement);
            
        } else if ( capturedStatement instanceof ForkStatement ) {
            ForkStatement forkStmt = (ForkStatement) capturedStatement;
            forkStmt.setForkingBThread(bss);
            
            final ScriptableObject globalScope = jsContext.initStandardObjects();
            try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BThreadSyncSnapshotOutputStream btos = new BThreadSyncSnapshotOutputStream(baos, globalScope) ) {
                btos.writeObject(cbs.getContinuation());
                btos.flush();
                baos.flush();
                forkStmt.setSerializedContinuation(baos.toByteArray());
                
            } catch (IOException ex) {
                Logger.getLogger(BPEngineTask.class.getName()).log(Level.SEVERE, "Error while serializing continuation during fork:" + ex.getMessage(), ex);
                throw new RuntimeException("Error while serializing continuation during fork:" + ex.getMessage(), ex);
            }
            
            listener.addFork(forkStmt);
            return continueParentOfFork(forkStmt, cbs, jsContext);
            
        } else {
            throw new IllegalStateException("Captured a statement of an unknown type: " + capturedStatement);
        }
    }
    
    private BThreadSyncSnapshot continueParentOfFork( ForkStatement forkStmt, ContinuationPending cbs, Context jsContext){
        try {
            System.out.println("Fork: " + forkStmt);
            jsContext.resumeContinuation(cbs.getContinuation(), 
                (Scriptable)cbs.getContinuation(), Undefined.instance);
            return null;
            
        } catch (ContinuationPending cbs2) {
            return handleContinuationPending(cbs2, jsContext);
           
        } catch ( WrappedException wfae ) {
            return handleWrappedException(wfae);
        }
    }
    
    private BThreadSyncSnapshot handleWrappedException(WrappedException wfae) throws WrappedException {
        if ( wfae.getCause() instanceof FailedAssertionException ) {
            FailedAssertionException fae = (FailedAssertionException) wfae.getCause();
            FailedAssertion fa = new FailedAssertion( fae.getMessage(), bss.getName() );
            listener.assertionFailed( fa );
            return null;
        } else {
            throw wfae;
        }
    }
    
}
