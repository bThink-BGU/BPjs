package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.internal.ContinuationProgramState;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeContinuation;
import org.mozilla.javascript.Scriptable;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

  /**
   * The changes to the b-program data this b-thread wants to make.
   * These changes are not part of the sync snapshot serialized form, since at
   * the serialization (i.e. after entering a sync point) all changes have been
   * applied to the program state, or we would have not been able to get into
   * the sync point due to conflict.
   */
  protected transient MapProxy<String, Object> bprogramStoreModifications;

  private transient ContinuationProgramState programState;

  /**
   * Fully detailed constructor. Mostly useful for getting objects out of
   * serialized forms.
   *
   * @param name             name of the b-thread
   * @param entryPoint       function where the b-thread starts
   * @param interruptHandler function to handle interrupts (or {@code null}, mostly)
   * @param continuation     captured b-thread continuation
   * @param bSyncStatement   current statement of the b-thread
   * @param someData         data for the b-thread (mqy be null).
   * @param modifications    modifications to the b-program store
   */
  public BThreadSyncSnapshot(String name, Function entryPoint, Function interruptHandler,
                             Object continuation, SyncStatement bSyncStatement, Object someData, MapProxy<String, Object> modifications) {
    this.name = name;
    this.entryPoint = entryPoint;
    this.interruptHandler = interruptHandler;
    this.continuation = (NativeContinuation) continuation;
    this.syncStatement = bSyncStatement;
    data = someData;
    bprogramStoreModifications = modifications;
  }

  public BThreadSyncSnapshot(String aName, Function anEntryPoint, BProgram bprog) {
    this(aName, anEntryPoint, null, null, null, null, new MapProxy(bprog.getStore()));
  }

  /**
   * Creates the next snapshot of the BThread, after {@code this} snapshot
   * ran.
   *
   * @param aContinuation        The BThread's continuation at the next sync.
   * @param aStatement           The BThread's statement for the next sync.
   * @param storageModifications storage modifications created during the run.
   * @return a copy of {@code this} with updated continuation and statement.
   */
  public BThreadSyncSnapshot makeNext(Object aContinuation, SyncStatement aStatement, MapProxy<String, Object> storageModifications) {
    BThreadSyncSnapshot retVal = new BThreadSyncSnapshot(name, null, interruptHandler,
        aContinuation, aStatement, data, storageModifications);

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

  public NativeContinuation getContinuation() {
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
    return (continuation != null) ? continuation : entryPoint;
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

  public void setBaseStore(Map<String, Object> aBaseStore) {
    bprogramStoreModifications.setSeed(aBaseStore);
  }

  public Map<String, MapProxy.Modification<Object>> getStorageModifications() {
    return bprogramStoreModifications.getModifications();
  }

  public void clearStorageModifications() {
    bprogramStoreModifications.reset();
  }

  /**
   * When {@code true}, this b-thread can continue. In cases where the return
   * value is {@code false}, this object holds data about the results of the
   * b-thread execution, e.g. storage modifications.
   *
   * @return {@code true} iff this b-thread can run another round.
   */
  public boolean canContinue() {
    return (getContinuation() != null) || (getEntryPoint() != null);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = prime * Objects.hash(name, syncStatement);
    if (continuation != null) {
      result += continuation.getImplementation().hashCode();
    }
    result += ScriptableUtils.jsHashCode(data);
    return result;
  }

  public ContinuationProgramState getContinuationProgramState() {
    if (programState == null) {
      programState = new ContinuationProgramState(continuation);
    }
    return programState;
  }

  @Override
  public boolean equals(Object obj) {
    // Quick circuit-breakers
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof BThreadSyncSnapshot)) return false;

    BThreadSyncSnapshot other = (BThreadSyncSnapshot) obj;

    if (!Objects.equals(getName(), other.getName())) {
      return false;
    }
    if (!Objects.equals(syncStatement, other.getSyncStatement())) {
      return false;
    }

    if ((!ScriptableUtils.jsEquals(data, other.getData())) ||
        (!Objects.equals(bprogramStoreModifications, other.bprogramStoreModifications))) {
      return false;
    }

    boolean michael = true;

    if (continuation == null) {
      // This b-thread hasn't run yet. Check eqality on its source.
      return (other.continuation == null) && entryPoint.equals(other.entryPoint);
    } else {
      if (michael) {
        // Michael's version: check equality on the PC+stack+heap
        return getContinuationProgramState().equals(other.getContinuationProgramState());
      } else {
        //Atila's version:
        return NativeContinuation.equalImplementations(continuation, other.getContinuation());
      }
    }
  }

  @Override
  public String toString() {
    return "[BThreadSyncSnapshot: " + name + " @" + hashCode() + "]";
  }

}
