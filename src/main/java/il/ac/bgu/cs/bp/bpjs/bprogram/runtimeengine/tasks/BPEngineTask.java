package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.tasks;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import java.util.concurrent.Callable;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * Base class for a parallel task executed during the execution of a {@link BProgram}.
 * Provides facilities for opening and closing the Javascript context, so that
 * sub-classes can just implement {@link #run(org.mozilla.javascript.Context)}
 * and forget about managing that.
 * 
 * @author moshewe
 * @author Michael
 */
public abstract class BPEngineTask implements Callable<BThreadSyncSnapshot>{
    
    private Context jsContext;
    
    @Override
    public BThreadSyncSnapshot call() throws Exception {
        try {
            jsContext = ContextFactory.getGlobal().enterContext();
            jsContext.setOptimizationLevel(-1); // must use interpreter mode
            return run(jsContext);
        } finally {
            Context.exit();
        }
    }
    
    protected abstract BThreadSyncSnapshot run(Context jsContext);

}
