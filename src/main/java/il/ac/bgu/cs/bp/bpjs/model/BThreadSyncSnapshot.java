package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.model.internal.ContinuationProgramState;
import java.io.Serializable;
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
    private Object continuation;

    /**
     * BSync statement of the BThread at the time of the snapshot.
     */
    private SyncStatement syncStatement;
    
    private transient ContinuationProgramState programState;

    public BThreadSyncSnapshot(String aName, Function anEntryPoint) {
        name = aName;
        entryPoint = anEntryPoint;
    }

    /**
     * Fully detailed constructor. Mostly useful for getting objects out of
     * serialized forms.
     *
     * @param name              name of the b-thread
     * @param entryPoint        function where the b-thread starts
     * @param interruptHandler  function to handle interrupts (or {@code null}, mostly)
     * @param continuation      captured b-thread continuation
     * @param bSyncStatement    current statement of the b-thread
     */
    public BThreadSyncSnapshot(String name, Function entryPoint, Function interruptHandler,
            Object continuation, SyncStatement bSyncStatement) {
        this.name = name;
        this.entryPoint = entryPoint;
        this.interruptHandler = interruptHandler;
        this.continuation = continuation;
        this.syncStatement = bSyncStatement;
    }

    /**
     * Creates the next snapshot of the BThread in a given run.
     *
     * @param aContinuation The BThread's continuation for the next sync.
     * @param aStatement The BThread's statement for the next sync.
     * @return a copy of {@code this} with updated continuation and statement.
     */
    public BThreadSyncSnapshot copyWith(Object aContinuation, SyncStatement aStatement) {
        BThreadSyncSnapshot retVal = new BThreadSyncSnapshot(name, entryPoint);
        retVal.continuation = aContinuation;
        retVal.setInterruptHandler(interruptHandler);
        retVal.syncStatement = aStatement;
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

    @Override
    public String toString() {
        return "[BThreadSyncSnapshot: " + name + " @" + hashCode() + "]";
    }

    public Optional<Function> getInterrupt() {
        return Optional.ofNullable(interruptHandler);
    }

    public void setInterruptHandler(Function anInterruptHandler) {
        interruptHandler = anInterruptHandler;
    }

    public Scriptable getScope() {
        return (continuation!=null) ? (Scriptable)continuation : entryPoint;
    }

    public Function getEntryPoint() {
        return entryPoint;
    }
    
    public ContinuationProgramState getContinuationProgramState() {
        if ( programState == null ) {
            programState = new ContinuationProgramState((NativeContinuation) continuation);
        }
        return programState;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * Objects.hash(name, syncStatement);
        if (continuation != null) {
            result += getContinuationProgramState().hashCode();
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
        if (continuation == null) {
            return (other.continuation == null);

        } else {
            return getContinuationProgramState().equals(other.getContinuationProgramState());
        }
    }

}
