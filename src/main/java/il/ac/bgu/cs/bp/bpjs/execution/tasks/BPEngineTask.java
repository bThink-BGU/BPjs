package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import java.util.concurrent.Callable;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;

/**
 * Base interface for a parallel task executed during the execution of a {@link BProgram}.

* @author Michael
 */
public interface BPEngineTask extends Callable<BThreadSyncSnapshot>{    
    
    @Override
    BThreadSyncSnapshot call() throws Exception;

}
