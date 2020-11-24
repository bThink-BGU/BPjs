package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.internal.MapProxy;
import il.ac.bgu.cs.bp.bpjs.model.internal.ContinuationProgramState;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeContinuation;
import org.mozilla.javascript.Scriptable;

import java.util.Objects;

/**
 * The state of a BThread at {@code bsync}.
 *
 * @author orelmosheweinstock
 * @author Michael
 */
public class BThreadSyncSnapshot implements Serializable {

    /**
     * Name of the BThread described
     */
    private String name;

    /**
     * The JavaScript function that will be called when {@code this} BThread
     * runs.
     */
    private final Function entryPoint;

    /**
     * BThreads may specify a function that runs when they are removed because
     * of a {@code breakUpon} statement.
     */
    private Function interruptHandler = null;

    /**
     * Continuation of the code.
     */
    private NativeContinuation continuation;

    /**
     * BSync statement of the BThread at the time of the snapshot.
     */
    private SyncStatement syncStatement;
    
    /**
     * BThread-global data object. Can be anything. For verification, needs
     * to be serializable, and with proper {@code equals} and {@code hashCode}.
     */
    private Object data;
    
    protected MapProxy<String, Object> bprogramStoreModifications;
    
    private transient ContinuationProgramState programState;

    /**
     * Fully detailed constructor. Mostly useful for getting objects out of
     * serialized forms.
     *
     * @param name              name of the b-thread
     * @param entryPoint        function where the b-thread starts
     * @param interruptHandler  function to handle interrupts (or {@code null}, mostly)
     * @param continuation      captured b-thread continuation
     * @param bSyncStatement    current statement of the b-thread
     * @param someData          data for the b-thread (mqy be null).
     * @param modifications     modifications to the b-program store
     */
    public BThreadSyncSnapshot(String name, Function entryPoint, Function interruptHandler,
            Object continuation, SyncStatement bSyncStatement, Object someData, MapProxy<String,Object> modifications) {
        this.name = name;
        this.entryPoint = entryPoint;
        this.interruptHandler = interruptHandler;
        this.continuation = (NativeContinuation)continuation;
        this.syncStatement = bSyncStatement;
        data = someData;
        bprogramStoreModifications = modifications;
    }

    public BThreadSyncSnapshot(String name, Function entryPoint, Function interruptHandler,
            Object continuation, SyncStatement bSyncStatement) {
        this(name, entryPoint, interruptHandler, continuation, bSyncStatement, null, null);
    }
    
    
    public BThreadSyncSnapshot(String aName, Function anEntryPoint) {
        this(aName, anEntryPoint, null, null, null, null, null );
    }
    
    public BThreadSyncSnapshot(String aName, Object someData, Function anEntryPoint) {
        this(aName, anEntryPoint, null, null, null, someData, null );
    }

    /**
     * Creates the next snapshot of the BThread in a given run.
     *
     * @param aContinuation The BThread's continuation for the next sync.
     * @param aStatement The BThread's statement for the next sync.
     * @return a copy of {@code this} with updated continuation and statement.
     */
    public BThreadSyncSnapshot copyWith(Object aContinuation, SyncStatement aStatement) {
        BThreadSyncSnapshot retVal = new BThreadSyncSnapshot(name, entryPoint, interruptHandler, aContinuation, aStatement, data, null);
        
        aStatement.setBthread(retVal);

        return retVal;
    }

    public SyncStatement getSyncStatement() {
        return syncStatement;
    }

    public void setSyncStatement(SyncStatement stmt) {
        syncStatement = stmt;
        if (syncStatement.getBthread() != this) {
            syncStatement.setBthread(this);
        }
    }

    public Object getContinuation() {
        return continuation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<Function> getInterrupt() {
        return Optional.ofNullable(interruptHandler);
    }

    public void setInterruptHandler(Function anInterruptHandler) {
        interruptHandler = anInterruptHandler;
    }

    public Scriptable getScope() {
        return (continuation!=null) ? continuation : entryPoint;
    }

    public Function getEntryPoint() {
        return entryPoint;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, MapProxy.Modification<Object>> getBprogramStoreModifications() {
        return bprogramStoreModifications.getModifications();
    }
    
    public ContinuationProgramState getContinuationProgramState() {
        if ( programState == null ) {
            programState = new ContinuationProgramState(continuation);
        }
        return programState;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * Objects.hash(name, syncStatement);
        if (continuation != null) {
            result += getContinuationProgramState().hashCode(name);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        // Quick circuit-breakers
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BThreadSyncSnapshot)) {
            return false;
        }
        BThreadSyncSnapshot other = (BThreadSyncSnapshot) obj;
        
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        if ( ! Objects.equals(syncStatement,other.getSyncStatement()) ) {
            return false;
        }
        
        if ( (!Objects.equals(data,other.getData())) ||
              (!Objects.equals(bprogramStoreModifications,other.bprogramStoreModifications)) ) {
            return false;
        }
        
        if (continuation == null) {
            // This b-thread hasn't run yet. Check eqality on its source.
            return (other.continuation == null) && entryPoint.equals(other.entryPoint);
        } else {
            // Check equality on the PC+stack+heap
            return getContinuationProgramState().equals(other.getContinuationProgramState());
        }
    }

    @Override
    public String toString() {
        return "[BThreadSyncSnapshot: " + name + " @" + hashCode() + "]";
    }

}
