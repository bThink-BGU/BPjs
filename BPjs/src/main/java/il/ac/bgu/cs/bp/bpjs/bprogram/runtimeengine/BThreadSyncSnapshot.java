package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import org.mozilla.javascript.*;

import java.io.Serializable;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy.BThreadJsProxy;
import java.util.Optional;

/**
 * The state of a BThread at {@code bsync}.
 *
 * @author orelmosheweinstock
 * @author Michael
 */
public class BThreadSyncSnapshot implements Serializable {

    /** Name of the BThread described */
    private String name;

    /**
     * The Javascript function that will be called when {@code this} BThread runs.
     */
    private Function entryPoint;

    /**
     * BThreads may specify a function that runs when they are removed because
     * of a {@code breakUpon} statement.
     */
    private Function interruptHandler = null;
    
    /** Proxy to {@code this}, used from the Javascript code.*/
    private final BThreadJsProxy proxy = new BThreadJsProxy(this);

    /** Scope for the Javascript code execution. */
    private Scriptable scope;

    /** Continuation of the code. */
    private Object continuation;
    
    /** BSync statement of the BThread at the time of the snapshot. */
    private BSyncStatement bSyncStatement;
    
    public BThreadSyncSnapshot(String aName, Function anEntryPoint) {
        name = aName;
        entryPoint = anEntryPoint;
    }

    /**
     * Convenience constructor with default parameters.
     */
    public BThreadSyncSnapshot() {
        this(BThreadSyncSnapshot.class.getName(), null);
    }
    
    /**
     * Creates the next snapshot of the BThread in a given run. 
     * @param aContinuation The BThread's continuation for the next sync.
     * @param aStatement The BThread's statement for the next sync.
     * @return a copy of {@code this} with updated continuation and statement.
     */
    public BThreadSyncSnapshot copyWith( Object aContinuation, BSyncStatement aStatement ) {
        BThreadSyncSnapshot retVal = new BThreadSyncSnapshot(name, entryPoint);
        retVal.continuation = aContinuation;
        retVal.setInterruptHandler(interruptHandler);
        retVal.setupScope(scope.getParentScope());

        retVal.bSyncStatement = aStatement;
        aStatement.setBthread(retVal);
        
        return retVal;
    }
    
    void setupScope(Scriptable programScope) {
        scope = (Scriptable) Context.javaToJS(proxy, programScope);
        scope.setPrototype(programScope);

        // This is a break from JS's semantics, but we have to do it.
        // In JS, inner functions know about variables in their syntactical parents.
        // For BThread functions we break this, and make them a top-level scope. This
        // works for us since the only communication between BThreads is via events,
        // so in particular they can't share variables.
        entryPoint.setParentScope(scope);
    }

    /**
     * Called from the Javascript code (via the proxy) when the client code wants to bsync.
     * @param aStatement 
     */
    public void bsync( BSyncStatement aStatement ) {
        bSyncStatement = aStatement.setBthread(this);
        ContinuationPending capturedContinuation = Context.getCurrentContext().captureContinuation();
        capturedContinuation.setApplicationState(aStatement);
        throw capturedContinuation;
    }
    
    public BSyncStatement getBSyncStatement() {
        return bSyncStatement;
    }

    public void setBSyncStatement(BSyncStatement stmt) {
        bSyncStatement = stmt;
        if ( bSyncStatement.getBthread() != this ) {
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
        return "[BThread: " + name + "]";
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
    
}
