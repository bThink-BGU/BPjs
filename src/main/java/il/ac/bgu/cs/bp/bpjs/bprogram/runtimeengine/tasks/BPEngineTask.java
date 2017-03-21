package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.tasks;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import java.util.concurrent.Callable;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;

/**
 * Base interface for a parallel task executed during the execution of a {@link BProgram}.

* @author Michael
 */
public interface BPEngineTask extends Callable<BThreadSyncSnapshot>{    
    
    @Override
    public BThreadSyncSnapshot call() throws Exception;

}
