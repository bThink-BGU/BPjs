package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import java.util.concurrent.Callable;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
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
        try {
            Context jsContext = Context.enter();
            
            return callImpl( jsContext );

        } catch (ContinuationPending cbs) {
            final BSyncStatement capturedStatement = (BSyncStatement) cbs.getApplicationState();
            capturedStatement.setBthread(bss);
            return bss.copyWith(cbs.getContinuation(), capturedStatement);
           
        } catch ( WrappedException wfae ) {
            if ( wfae.getCause() instanceof FailedAssertionException ) {
                FailedAssertionException fae = (FailedAssertionException) wfae.getCause();
                FailedAssertion fa = new FailedAssertion( fae.getMessage(), bss.getName() );
                listener.assertionFailed( fa );
                return null;
            } else {
                throw wfae;
            }
            
        } finally {
            if (Context.getCurrentContext() != null) {
                Context.exit();
            }
        }
    }
    
}
