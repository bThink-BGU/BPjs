package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.JsEventSet;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.FailedAssertionException;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.ForkStatement;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

/**
 * An object representing the {@link BProgram} context for JavaScript code.
 * Methods in this object allow JavaScript code to register new BThreads, 
 * create events,write messages to the log etc.
 * 
 * Methods in the class are available to JavaScript code via the {@code bp}
 * object, like so:
 * 
 * <pre><code>
 * bp.log.info("I'm a log message!");
 * var myEvent = bp.Event("My Event");
 * bp.registerBThread(...);
 * </code></pre>
 * 
 * @author michael
 */
public class BProgramJsProxy extends SyncStatementBuilder
                             implements java.io.Serializable {
    
    ///////////////////////////////
    // A (Java) thread-local mechanism to allow the Java code calling the BP 
    // code to communicate with the Java code called from the BP code.
    private static class BThreadData {
        final BThreadSyncSnapshot snapshot;
        final MapProxy storeModifications;

        public BThreadData(BThreadSyncSnapshot snapshot, MapProxy storeModifications) {
            this.snapshot = snapshot;
            this.storeModifications = storeModifications;
        }        
    }
    
    private static final ThreadLocal<BThreadData> CURRENT_BTHREAD = new ThreadLocal<>();
    
    
    public static void setCurrentBThread( BProgramSyncSnapshot bpss, BThreadSyncSnapshot btss ) {
        CURRENT_BTHREAD.set(new BThreadData( btss, new MapProxy(bpss.getDataStore())));
    }
    
    public static void clearCurrentBThread(){
        CURRENT_BTHREAD.remove();
    }
    
    public static MapProxy getCurrentChanges() {
        BThreadData bThreadData = CURRENT_BTHREAD.get();
        return (bThreadData != null) ? bThreadData.storeModifications : null;
    }
    
    // /thread-local
    ///////////////////////////////
    
    /**
     * State of a b-thread, captures during a bp.sync.
     */
    public static class CapturedBThreadState {
        public final SyncStatement syncStmt;
        public final MapProxy modifications;

        public CapturedBThreadState(SyncStatement syncStmt, MapProxy modifications) {
            this.syncStmt = syncStmt;
            this.modifications = modifications;
        }
    }
    
    
    private final BProgram bProg;
    
    private final AtomicInteger autoAddCounter = new AtomicInteger(0);
    
    public final BpLog log = new BpLog();
    
    /** Deprecated - use eventSets.all */
    @Deprecated(forRemoval = true)
    public final EventSet all = EventSets.all;
    
    /** Deprecated - use eventSets.none */
    @Deprecated(forRemoval = true)
    public final EventSet none = EventSets.none;
    
    public final EventSetsJsProxy eventSets = new EventSetsJsProxy();
    
    /**
     * Facility for creating random numbers. BPjs code should not use JavaScript's
     * random facility, as it won't play well with model checking.
     */
    public RandomProxy random = new RandomProxy();
    
    public BProgramJsProxy(BProgram aBProgram) {
        bProg = aBProgram;
    }
    
    /**
     * Event constructor, called from JavaScript, hence the funny
     * capitalization.
     *
     * @param name name of the event
     * @return an event with the passed name.
     */
    public BEvent Event(String name) {
        return new BEvent(name);
    }

    /**
     * Event constructor, called from JavaScript, hence the funny
     * capitalization.
     *
     * @param name name of the event
     * @param jsData Additional data for the object.
     * @return an event with the passed name.
     */
    public BEvent Event(String name, Object jsData) {
        return new BEvent(name, jsData );
    }
    
    public JsEventSet EventSet(String name, Object predicateObj) {
        if ( predicateObj instanceof Function ) {
            return new JsEventSet(name, (Function) predicateObj);
        } else {
            throw new BPjsRuntimeException("An event set predicate has to be a function.");
        }
    }
    
    public EventSet allExcept( EventSet es ) {
        return EventSets.not(es);
    }
    
    /**
     * Called from JS to add BThreads with data.
     *
     * @param name Name of the registered BThread (useful for debugging).
     * @param data Data object for the b-thread.
     * @param func Script entry point of the BThread.
     *
     * @see #registerBThread(org.mozilla.javascript.Function)
     */
    public void registerBThread(String name, Object data, Function func) {
        bProg.registerBThread(new BThreadSyncSnapshot(name, func, null, null, null, data, null));
    }
    
    /**
     * Called from JS to add BThreads running function as their runnable code.
     *
     * @param name Name of the registered BThread (useful for debugging).
     * @param func Script entry point of the BThread.
     *
     * @see #registerBThread(org.mozilla.javascript.Function)
     */
    public void registerBThread(String name, Function func) {
        bProg.registerBThread(new BThreadSyncSnapshot(name, func, bProg));
    }

    /**
     * Registers a BThread and gives it a unique name. Use when you don't care
     * about the added BThread's name.
     *
     * @param func the BThread to add.
     *
     * @see #registerBThread(java.lang.String, org.mozilla.javascript.Function)
     */
    public void registerBThread(Function func) {
        registerBThread("autoadded-" + autoAddCounter.incrementAndGet(), func);
    }
    
    /**
     * If {@code value} is {@code false}, puts the entire program in an invalid
     * state. This, in turn, would terminate it when it's being run, or discover 
     * a specification violation when it's being verified.
     * 
     * note: I'd rather call it {@code assert} too, but that's a Java keyword, which complicates stuff.
     * 
     * @param value The value of the assertion. When {@code false}, the program is declared in invalid state.
     * @param message Textual information about what caused the violation.
     * @throws FailedAssertionException if {@code value} is false.
     */
    public void ASSERT( boolean value, String message ) throws FailedAssertionException {
        if ( ! value ) {
            throw new FailedAssertionException( message );
        }
    }
    
    public void fork() throws ContinuationPending {
        ContinuationPending capturedContinuation = Context.getCurrentContext().captureContinuation();
        capturedContinuation.setApplicationState(new ForkStatement(capturedContinuation.getContinuation()));
        throw capturedContinuation;
    }
    
    
    public void setInterruptHandler( Object aPossibleHandler ) {
        CURRENT_BTHREAD.get().snapshot.setInterruptHandler(
                (aPossibleHandler instanceof Function) ? (Function) aPossibleHandler: null );
    }
    
    ////////////////////////
    // sync ("bp.sync") related code
    
    @Override
    public void sync( NativeObject jsRWB, Object data ) {
        synchronizationPoint(jsRWB, null, data);
    }
    
    @Override
    public SyncStatementBuilder hot(boolean isHot) {
        SyncStatementBuilderImpl sub = new SyncStatementBuilderImpl(this);
        sub.setHotness(isHot);
        
        return sub;
    }
    
    /**
     * Where the actual Behavioral Programming synchronization point is done.
     * 
     * @param jsRWB The JavaScript object {@code {request:... waitFor:...}} 
     * @param hot   {@code True} if this should be a "hot" synchronization point.
     * @param data Optional extra data the synchronizing b-thread may want to add.
     */
    @Override
    void synchronizationPoint( NativeObject jsRWB, Boolean hot, Object data ) {
        Map<String, Object> jRWB = (Map)Context.jsToJava(jsRWB, Map.class);
        
        SyncStatement stmt = SyncStatement.make();
        if ( hot != null ) {
            stmt = stmt.hot(hot);
        }
        Object req = jRWB.get("request");
        if ( req != null ) {
            try { 
                if ( req instanceof NativeArray ) {
                    NativeArray arr = (NativeArray) req;
                    stmt = stmt.request(arr.getIndexIds().stream()
                                           .map( i -> (BEvent)arr.get(i) )
                                           .collect( toList() ));
                } else {
                    stmt = stmt.request((BEvent)req);
                }
            } catch (ClassCastException cce ) {
                throw new BPjsRuntimeException("A non-event object requested in a sync statement. Offending object:'" + ScriptableUtils.stringify(req) + "'");
            }
        }

        EventSet waitForSet   = convertToEventSet(jRWB.get("waitFor"));
        EventSet blockSet     = convertToEventSet(jRWB.get("block"));
        EventSet interruptSet = convertToEventSet(jRWB.get("interrupt"));
        stmt = stmt.waitFor( waitForSet )
                     .block( blockSet )
                 .interrupt( interruptSet )
                      .data( data );
       
        captureBThreadState(stmt);
    }

    private EventSet convertToEventSet( Object jsObject ) {
        if ( jsObject == null ) return EventSets.none;
        
        // This covers event sets AND events.
        if ( jsObject instanceof EventSet ) {
            return (EventSet)jsObject;
        
        } else if ( jsObject instanceof NativeArray ) {
            NativeArray arr = (NativeArray) jsObject;
            if ( arr.isEmpty() ) return EventSets.none;
            
            if ( Stream.of(arr.getIds()).anyMatch( id -> arr.get(id)==null) ) {
                throw new RuntimeException("EventSet Array contains null sets.");
            }
            
            if ( arr.getLength() == 1 ) return (EventSet)arr.get(0);
            
            return EventSets.anyOf(
                arr.getIndexIds().stream()
                    .map( i ->(EventSet)arr.get(i) )
                    .collect( toSet() ) );
        } else {
            final String errorMessage = "Cannot convert " + jsObject + " of class " + jsObject.getClass() + " to an event set";
            throw new BPjsRuntimeException(errorMessage);
        }
    }
    
    private void captureBThreadState(SyncStatement stmt) throws ContinuationPending {
        ContinuationPending capturedContinuation = Context.getCurrentContext().captureContinuation();
        BThreadData curBThreadData = CURRENT_BTHREAD.get();
        capturedContinuation.setApplicationState(new CapturedBThreadState(stmt, 
            curBThreadData!=null ? curBThreadData.storeModifications : null));
        throw capturedContinuation;
    }
    
    // /sync
    /////////////////////////
    
    
    /**
     * Push a new event to the external event queue. 
     * @param evt The event to be pushed.
     * @return the event being pushed.
     */
    public BEvent enqueueExternalEvent( BEvent evt )  {
        bProg.enqueueExternalEvent(evt);
        return evt;
    }
    
    /**
     * Sets whether the BProgram will wait for external events when there's
     * no internal event to choose.
     * 
     * @param newDaemonMode {@code true} for making {@code this} a daemon; 
     *                      {@code false} otherwise.
     */
    public void setWaitForExternalEvents( boolean newDaemonMode ) {
        bProg.setWaitForExternalEvents( newDaemonMode );
    }
    
    public boolean isWaitForExternalEvents() {
        return bProg.isWaitForExternalEvents();
    }
    
    /**
     * @return Returns the current time in milliseconds since 1/1/1970.
     */
    public long getTime() {
        return System.currentTimeMillis();
    }
    
    
    public BThreadDataProxy getThread(){
        return new BThreadDataProxy(CURRENT_BTHREAD.get().snapshot);
    }
    
    public MapProxy getStore() {
        BThreadData bThreadExecutionContext = CURRENT_BTHREAD.get();
        
        return (bThreadExecutionContext!=null) ? bThreadExecutionContext.storeModifications
            : new DirectMapProxy<>(bProg.getStore());
    }
    
    /**
     * Gets the name of the Java thread executing this b-thread at the moment. Useful for
     * debugging Java runtime issues.
     * 
     * @return the name of the Java thread executing this b-thread at the moment.
     */
    public String getJavaThreadName() {
        return Thread.currentThread().getName();
    }
    
    @Override
    public int hashCode() {
        return 42;
    }

    /**
     * Proxies contain no state of their own, and provide a gateway to the Java
     * environment the BProgram runs in. When comparing sync snapshots, we can 
     * encounter JS proxies when traversing the JS heap and stack. To prevent leaking
     * the equality evaluation outside of the JavaScript, we do not check the 
     * equality of the {@link BProgram} {@code this} is a proxy of. Rather, we 
     * just check that the other object is a proxy too.
     * 
     * @param obj
     * @return {@code true} iff the other object is a {@code BProgramJsProxy}.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        
        return getClass() == obj.getClass();
    }

}
