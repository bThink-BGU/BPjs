package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.bprogramio.log.BpLog;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsCodeEvaluationException;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsException;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy.CapturedBThreadState;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import java.util.concurrent.Callable;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertionViolation;
import il.ac.bgu.cs.bp.bpjs.model.ForkStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import java.util.Arrays;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
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
        public void assertionFailed( FailedAssertionViolation fa );
        public void addFork( ForkStatement stmt );
    }
    
    protected final BThreadSyncSnapshot btss;
    protected final BProgramSyncSnapshot bpss;
    protected final Listener listener;

    BPEngineTask(BProgramSyncSnapshot aBpss, BThreadSyncSnapshot aBtss, Listener aListener) {
        bpss = aBpss;
        btss = aBtss;
        listener = aListener;
    }
    
    abstract void callImpl(Context jsContext);
    
    @Override
    public BThreadSyncSnapshot call() {

        Context jsContext = BPjs.enterRhinoContext();
        try {            
            BProgramJsProxy.setCurrentBThread(bpss, btss);
            callImpl( jsContext );
            // capture proxy changes
            MapProxy<String,Object> changes = BProgramJsProxy.getCurrentChanges();
            if ( changes != null ) {
                if ( ! changes.getModifications().isEmpty() ) {
                    return new BThreadSyncSnapshot(btss.getName(), null, null, null, null, null, changes);
                }
            } 
            return null;

        } catch (ContinuationPending cbs) {
            return handleContinuationPending(cbs, jsContext);
           
        } catch ( WrappedException wfae ) {
            return handleWrappedException(wfae);
            
        } catch ( EvaluatorException | JavaScriptException eve ) {
            throw new BPjsCodeEvaluationException(eve);
            
        } catch ( EcmaError jsError ) {
            if ( jsError.getMessage().startsWith("ReferenceError") ) {
                throw new BPjsCodeEvaluationException(jsError);
            } else {
                throw new BPjsRuntimeException("JavaScript error: " + jsError.getMessage(), jsError);            
            }
            
        } catch ( Throwable generalThrowable ) {
            final BpLog log = BPjs.log();
            log.error("BPjs Error: Unhandled exception in BPEngineTask.");
            log.error("            This is a bug in BPjs. Please report. Sorry.");
            log.error(generalThrowable.getMessage());
            Throwable curThrowable = generalThrowable;
            while ( curThrowable != null ) {
                if ( curThrowable.getStackTrace() != null ) {
                    Arrays.asList(curThrowable.getStackTrace()).forEach( frame -> {
                       log.error( String.format(" at %s (%s:%d)", frame.getClassName(), frame.getFileName(), frame.getLineNumber()));
                    });
                }
                curThrowable = curThrowable.getCause();
                if ( curThrowable != null ) {
                    log.error("Caused by:");
                }
            }
            
            throw generalThrowable;
            
        } finally {
            Context.exit();
            BProgramJsProxy.clearCurrentBThread();
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
        
        if ( capturedStatement instanceof CapturedBThreadState ) {
            final CapturedBThreadState capturedState = (CapturedBThreadState) cbs.getApplicationState();
            
            SyncStatement syncStatement = capturedState.syncStmt;
            // warn on self-blocking
            boolean hasRequest = ! syncStatement.getRequest().isEmpty();
            boolean hasBlock   = (syncStatement.getBlock() != EventSets.none );
            if ( hasRequest && hasBlock ) {
                boolean hasCollision = syncStatement.getRequest().stream().allMatch(syncStatement.getBlock()::contains);
                if ( hasCollision ) {
                    BPjs.log().warn("B-thread '"+btss.getName()+"' is blocking an event it is also requesting, this may lead to a deadlock.");
                }
            }
            
            return btss.makeNext(cbs.getContinuation(), syncStatement, capturedState.modifications);
            
        } else if ( capturedStatement instanceof ForkStatement ) {
            ForkStatement forkStmt = (ForkStatement) capturedStatement;
            forkStmt.setForkingBThread(btss);
            forkStmt.cloneBThreadData(bpss); // and then there were two
            listener.addFork(forkStmt);
            
            return continueParentOfFork(cbs, jsContext);
                        
        } else {
            
            throw new IllegalStateException("Captured a statement of an unknown type: " + capturedStatement);
        }
    }
    
    private BThreadSyncSnapshot continueParentOfFork( ContinuationPending cbs, Context jsContext){
        try {
            jsContext.resumeContinuation(cbs.getContinuation(), 
                (Scriptable)cbs.getContinuation(), Undefined.instance);
            return null;
            
        } catch ( ContinuationPending cbs2 ) {
            return handleContinuationPending(cbs2, jsContext);
           
        } catch ( WrappedException wfae ) {
            return handleWrappedException(wfae);
        }
    }
    
    private BThreadSyncSnapshot handleWrappedException(WrappedException wfae) throws WrappedException {
        if ( wfae.getCause() instanceof FailedAssertionException ) {
            FailedAssertionException fae = (FailedAssertionException) wfae.getCause();
            FailedAssertionViolation fa = new FailedAssertionViolation( fae.getMessage(), btss.getName() );
            listener.assertionFailed( fa );
            return null;
            
        } else if (wfae.getCause() instanceof BPjsException) {
            throw (BPjsException)wfae.getCause();
            
        } else {
            throw wfae;
        }
    }
    
}
