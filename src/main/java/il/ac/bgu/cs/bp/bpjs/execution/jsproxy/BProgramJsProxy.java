package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.JsEventSet;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.FailedAssertionException;
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContinuationPending;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

/**
 * An object representing the {@link BProgram} context for Javascript code.
 * Methods in this object allow Javascript code to register new BThreads, 
 * create events,write messages to the log etc.
 * 
 * Methods in the class are available to Javascript code via the {@code bp}
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
public class BProgramJsProxy implements java.io.Serializable {
    
    
    private final BProgram program;
    
    private final AtomicInteger autoAddCounter = new AtomicInteger(0);
    
    public final BpLog log = new BpLog();
    
    public final EventSet all = EventSets.all;
    
    public final EventSet none = EventSets.none;
    
    /**
     * Facility for creating random numbers. BPjs code should not use Javascript's
     * random facility, as it won't play well with model checking.
     */
    public RandomProxy random = new RandomProxy();
    
    public BProgramJsProxy(BProgram program) {
        this.program = program;
    }
    
    /**
     * Event constructor, called from Javascript, hence the funny
     * capitalization.
     *
     * @param name name of the event
     * @return an event with the passed name.
     */
    public BEvent Event(String name) {
        return new BEvent(name);
    }

    /**
     * Event constructor, called from Javascript, hence the funny
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
        return EventSets.allExcept(es);
    }
    
    /**
     * Called from JS to add BThreads running func as their runnable code.
     *
     * @param name Name of the registered BThread (useful for debugging).
     * @param func Script entry point of the BThread.
     *
     * @see #registerBThread(org.mozilla.javascript.Function)
     */
    public void registerBThread(String name, Function func) {
        program.registerBThread(new BThreadSyncSnapshot(name, func));
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
     * @throws FailedAssertionException 
     */
    public void ASSERT( boolean value, String message ) throws FailedAssertionException {
        if ( ! value ) {
            throw new FailedAssertionException( message );
        }
    }
    
    
    ////////////////////////
    // sync ("bsync") related code
    
    public void sync( NativeObject jsRWB ) {
        sync(jsRWB, null);
    }
    
    public void sync( NativeObject jsRWB, Object data ) {
        Map<String, Object> jRWB = (Map)Context.jsToJava(jsRWB, Map.class);
        
        BSyncStatement stmt = BSyncStatement.make();
        Object req = jRWB.get("request");
        if ( req != null ) {
            if ( req instanceof BEvent ) {
                stmt = stmt.request((BEvent)req);
            } else if ( req instanceof NativeArray ) {
                NativeArray arr = (NativeArray) req;
                stmt = stmt.request(
                        Arrays.asList( arr.getIndexIds() ).stream()
                              .map( i -> (BEvent)arr.get(i) )
                              .collect( toList() ));
            } 
        }

        EventSet waitForSet = convertToEventSet(jRWB.get("waitFor"));
        EventSet blockSet = convertToEventSet(jRWB.get("block"));
        EventSet interruptSet = convertToEventSet(jRWB.get("interrupt"));
        stmt = stmt.waitFor( waitForSet )
                     .block( blockSet )
                 .interrupt( interruptSet )
                      .data( data );
        boolean hasCollision = stmt.getRequest().stream().anyMatch(blockSet::contains);
        if (hasCollision) {
            System.err.println("Warning: BThread is blocking an event it is also requesting, this may lead to a deadlock.");
        }
        captureBThreadState(stmt);
        
    }

    private EventSet convertToEventSet( Object jsObject ) {
        if ( jsObject == null ) return EventSets.none;
        
        // This covers event sets AND events.
        if ( jsObject instanceof EventSet ) {
            return (EventSet)jsObject;
        
        } else if ( jsObject instanceof NativeArray ) {
            NativeArray arr = (NativeArray) jsObject;
            if ( Stream.of(arr.getIds()).anyMatch( id -> arr.get(id)==null) ) {
                throw new RuntimeException("EventSet Array contains null sets.");
            }
            return ComposableEventSet.anyOf(
              Arrays.asList(arr.getIndexIds()).stream()
                    .map( i ->(EventSet)arr.get(i) )
                    .collect( toSet() ) );
        } else {
            final String errorMessage = "Cannot convert " + jsObject + " of class " + jsObject.getClass() + " to an event set";
            Logger.getLogger(BThreadSyncSnapshot.class.getName()).log(Level.SEVERE, errorMessage);
            throw new IllegalArgumentException( errorMessage);
        }
    }
    
    private void captureBThreadState(BSyncStatement stmt) throws ContinuationPending {
        ContinuationPending capturedContinuation = Context.getCurrentContext().captureContinuation();
        capturedContinuation.setApplicationState(stmt);
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
        program.enqueueExternalEvent(evt);
        return evt;
    }
    
    /**
     * Sets whether the BProgram will wait for external events when there's
     * no internal event to choose.
     * 
     * @param newDaemonMode {@code true} for making {@code this} a daemon; 
     *                      {@code false} otherwise.
     */
    public void setDaemonMode( boolean newDaemonMode ) {
        program.setWaitForExternalEvents( newDaemonMode );
    }
    
    public boolean isDaemonMode() {
        return program.isWaitForExternalEvents();
    }
    
    /**
     * Loads a Javascript resource (a file that's included in the .jar).
     *
     * @param path absolute path of the resource in the .jar file.
     */
    public void loadJavascriptResource(String path) {
        program.evaluateResource(path);
    }
    
    /**
     * @return Returns the current time in milliseconds since 1/1/1970.
     */
    public long getTime() {
        return System.currentTimeMillis();
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
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.program);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BProgramJsProxy other = (BProgramJsProxy) obj;
        return Objects.equals(this.program, other.program);
    }
    
    
}
