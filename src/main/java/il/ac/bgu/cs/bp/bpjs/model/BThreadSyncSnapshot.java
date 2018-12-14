package il.ac.bgu.cs.bp.bpjs.model;

import java.io.Serializable;
import java.util.Optional;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeContinuation;
import org.mozilla.javascript.Scriptable;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BThreadJsProxy;
import il.ac.bgu.cs.bp.bpjs.analysis.ContinuationProgramState;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import java.util.Objects;
import org.mozilla.javascript.ScriptableObject;

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
     * The Javascript function that will be called when {@code this} BThread
     * runs.
     */
    private final Function entryPoint;

    /**
     * BThreads may specify a function that runs when they are removed because
     * of a {@code breakUpon} statement.
     */
    private Function interruptHandler = null;

    /**
     * Proxy to {@code this}, used from the Javascript code.
     */
    private final BThreadJsProxy proxy = new BThreadJsProxy(this);

    /**
     * Scope for the JavaScript code execution.
     */
    private Scriptable scope;

    /**
     * Continuation of the code.
     */
    private Object continuation;

    /**
     * BSync statement of the BThread at the time of the snapshot.
     */
    private SyncStatement bSyncStatement;
    
    private transient ContinuationProgramState programState;

    public BThreadSyncSnapshot(String aName, Function anEntryPoint) {
        name = aName;
        entryPoint = anEntryPoint;
    }

    /**
     * Fully detailed constructor. Mostly useful for getting objects out of
     * serialized forms.
     *
     * @param name
     * @param entryPoint
     * @param interruptHandler
     * @param scope
     * @param continuation
     * @param bSyncStatement
     */
    public BThreadSyncSnapshot(String name, Function entryPoint, Function interruptHandler, Scriptable scope,
            Object continuation, SyncStatement bSyncStatement) {
        this.name = name;
        this.entryPoint = entryPoint;
        this.interruptHandler = interruptHandler;
        this.scope = scope;
        this.continuation = continuation;
        this.bSyncStatement = bSyncStatement;
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
        retVal.setupScope(ScriptableObject.getTopLevelScope(scope));

        retVal.bSyncStatement = aStatement;
        aStatement.setBthread(retVal);

        return retVal;
    }

    /**
     * Setting up the scope like so:
     * <code>
     *  
     * entryPoint -> ... -> top level scope -> bthread -> bprogram
     * 
     * </code>
     * @param programScope 
     */
    void setupScope(Scriptable programScope) {
        
        Scriptable bthreadProxyScope = (Scriptable) Context.javaToJS(proxy, programScope);
        bthreadProxyScope.delete("equals");
        bthreadProxyScope.delete("hashCode");
        bthreadProxyScope.delete("toString");
        bthreadProxyScope.delete("toString");
        bthreadProxyScope.delete("wait");
        bthreadProxyScope.delete("notify");
        bthreadProxyScope.delete("notifyAll");
        
        bthreadProxyScope.setParentScope(programScope);
        
        // setup entryPoint's scope s.t. it knows our proxy
        Scriptable penultimateEntryPointScope = ScriptableUtils.getPenultiamteParent(entryPoint);
        penultimateEntryPointScope.setParentScope(bthreadProxyScope);
        
        scope = entryPoint;
        
    }

    public SyncStatement getBSyncStatement() {
        return bSyncStatement;
    }

    public void setBSyncStatement(SyncStatement stmt) {
        bSyncStatement = stmt;
        if (bSyncStatement.getBthread() != this) {
            bSyncStatement.setBthread(this);
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
        return "[BThreadSyncSnapshot: " + name + "]";
    }

    public Optional<Function> getInterrupt() {
        return Optional.ofNullable(interruptHandler);
    }

    public void setInterruptHandler(Function anInterruptHandler) {
        interruptHandler = anInterruptHandler;
    }

    public Scriptable getScope() {
        return scope;
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
        int result = 1;
        result = prime * result + Objects.hashCode(name.hashCode());
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

        if (continuation == null) {
            return (other.continuation == null);

        } else {
            return getContinuationProgramState().equals(other.getContinuationProgramState());
        }
    }

}
