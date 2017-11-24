package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import java.io.Serializable;
import java.util.Optional;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeContinuation;
import org.mozilla.javascript.Scriptable;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy.BThreadJsProxy;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.ContinuationProgramState;
import java.util.Objects;

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
	 * BThreads may specify a function that runs when they are removed because of a
	 * {@code breakUpon} statement.
	 */
	private Function interruptHandler = null;

	/** Proxy to {@code this}, used from the Javascript code. */
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
			Object continuation, BSyncStatement bSyncStatement) {
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
	 * @param aContinuation
	 *            The BThread's continuation for the next sync.
	 * @param aStatement
	 *            The BThread's statement for the next sync.
	 * @return a copy of {@code this} with updated continuation and statement.
	 */
	public BThreadSyncSnapshot copyWith(Object aContinuation, BSyncStatement aStatement) {
		BThreadSyncSnapshot retVal = new BThreadSyncSnapshot(name, entryPoint);
		retVal.continuation = aContinuation;
		retVal.setInterruptHandler(interruptHandler);
		retVal.setupScope(scope.getParentScope());

		retVal.bSyncStatement = aStatement;
		aStatement.setBthread(retVal);

		return retVal;
	}

	/**
	 * Runs the b-thread from the start to the first {@code bsync}. If there are no
	 * calls to {@code bsync}, runs to completion.
	 * 
	 * @return a snapshot of the b-thread after the first {@code bsync} call, or
	 *         {@code null} in case no such call exists.
	 */
	public BThreadSyncSnapshot startBThread() {
		try {
			Context jsContext = Context.enter();
			jsContext.callFunctionWithContinuations(getEntryPoint(), getScope(), new Object[0]);
			return null;

		} catch (ContinuationPending cbs) {
			return copyWith(cbs.getContinuation(), (BSyncStatement) cbs.getApplicationState());

		} finally {
			Context.exit();
		}
	}

	/**
	 * Makes the call to {@code bsync} on which the b-thread snapshot was made
	 * return the passed event. Then returns the snapshot at the next {@code bsync},
	 * if any.
	 * 
	 * @param anEvent
	 *            The event to trigger
	 * @return snapshot of the bthread at the next call to {@code bsync}, or
	 *         {@code null}, if the b-thread ran to completion.
	 */
	public BThreadSyncSnapshot triggerEvent(BEvent anEvent) {
		try {
			Context jsContext = Context.enter();
			Object toResume = getContinuation();
			Object eventInJS = Context.javaToJS(anEvent, getScope());
			jsContext.resumeContinuation(toResume, getScope(), eventInJS);
			return null;

		} catch (ContinuationPending cbs) {
			return copyWith(cbs.getContinuation(), (BSyncStatement) cbs.getApplicationState());
		} finally {
            if (Context.getCurrentContext() != null ) {
    			Context.exit();
            }
		}
	}

	void setupScope(Scriptable programScope) {
		scope = (Scriptable) Context.javaToJS(proxy, programScope);
		scope.delete("equals");
		scope.setParentScope(programScope);

		Scriptable curScope = entryPoint.getParentScope();
		if (curScope.getParentScope() == null) {
			entryPoint.setParentScope(scope);
			scope.setParentScope(curScope);
		} else {
			while (curScope.getParentScope().getParentScope() != null) {
				curScope = curScope.getParentScope();
			}
			scope.setParentScope(curScope.getParentScope());
			curScope.setParentScope(scope);
		}
	}

	public BSyncStatement getBSyncStatement() {
		return bSyncStatement;
	}

	public void setBSyncStatement(BSyncStatement stmt) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
        // Quick circuit-breakers
        if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof BThreadSyncSnapshot)) return false;
        BThreadSyncSnapshot other = (BThreadSyncSnapshot) obj;
		if ( ! Objects.equals(getName(), other.getName())) return false;
        
		if (continuation == null) {
			return (other.continuation == null);
                    
		} else {
			NativeContinuation natCont = (NativeContinuation) continuation;
			NativeContinuation natOtherCont = (NativeContinuation) other.continuation;
			return new ContinuationProgramState(natCont).equals(new ContinuationProgramState(natOtherCont));
		}
	}

}
