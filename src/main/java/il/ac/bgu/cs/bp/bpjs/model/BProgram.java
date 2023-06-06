package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BpLog;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsCodeEvaluationException;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsException;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.FailedAssertionException;
import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import java.util.concurrent.atomic.AtomicInteger;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.WrappedException;

/**
 * Base class for BPrograms. Provides the context (JavaScript scope, external
 * event queue, etc.) b-threads interact with while running. Concrete BProgram
 * extend this class by implementing the
 * {@link #setupProgramScope(org.mozilla.javascript.Scriptable)} method.
 *
 * For creating a BProgram that uses a single JavaScript file available in the
 * classpath, see {@link ResourceBProgram}. For creating them from a
 * hard-coded string, see {@link StringBProgram}.
 *
 * @author michael
 */
public abstract class BProgram {

    // ------------- Static Members ---------------
    /**
     * "Poison pill" to insert to the external event queue. Used only to turn
     * the wait-for-external-events mode off.
     */
    static final BEvent NO_MORE_WAIT_EXTERNAL = new BEvent("___bpjs-internal____NO_MORE_WAIT_EXTERNAL");

    /**
     * Counter for giving anonymous instances some semantic name.
     */
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    private BpLog preSetLogger = null;

    /**
     * A callback interface invoked when a b-thread is added to {@code this}.
     */
    public interface BProgramCallback {

        void bthreadAdded(BProgram bp, BThreadSyncSnapshot theBThread);
    }

    // ------------- Instance Members ---------------
    private String name;

    /**
     * When {@code true}, the BProgram waits for an external event when no
     * internal ones are available. Corollary: the program does not terminate at
     * the end of a super-step.
     */
    private boolean waitForExternalEvents;

    /**
     * Events are enqueued here by external threads
     */
    private final BlockingQueue<BEvent> recentlyEnqueuedExternalEvents = new LinkedBlockingQueue<>();

    /**
     * BThreads added between syncs are added here.
     */
    private final BlockingQueue<BThreadSyncSnapshot> recentlyRegisteredBthreads = new LinkedBlockingDeque<>();
    
    private final BlockingQueue<ForkStatement> recentlyAddedForks = new LinkedBlockingDeque<>();

    private volatile boolean started = false;

    protected Scriptable programScope;

    private EventSelectionStrategy eventSelectionStrategy;
    
    private StorageModificationStrategy storageModificationStrategy = StorageModificationStrategy.PASSTHROUGH;

    private BProgramJsProxy jsProxy;
    
    private BpLog.LogLevel preSetLogLevel = null;
    
    private PrintStream preSetPrintStream = null;

    /**
     * Objects that client code wishes to put in scope before the scope is
     * initialized are collected here.
     */
    protected Map<String, Object> initialScopeValues = new HashMap<>();
    
    
    protected Map<String, Object> initialStore = new HashMap<>();

    private Optional<BProgramCallback> addBThreadCallback = Optional.empty();

    private List<String> appendedCode;
    private List<String> prependedCode;

    /**
     * Constructs a BProgram with a default name, guaranteed to be unique within
     * a given run.
     */
    public BProgram() {
        this("BProgram-" + INSTANCE_COUNTER.incrementAndGet());
    }

    /**
     * Constructs a BProgram with a specific name.
     *
     * @param aName name for the new BProgram.
     */
    public BProgram(String aName) {
        name = aName;
    }

    /**
     * Creates a BProgram with a specific name and an event selection strategy.
     *
     * @param aName Name for the program.
     * @param anEss Event selection strategy.
     */
    public BProgram(String aName, EventSelectionStrategy anEss) {
        name = aName;
        eventSelectionStrategy = anEss;
    }

    /**
     * Adds more source code to be evaluated <em>after</em>
     * {@link #setupProgramScope(org.mozilla.javascript.Scriptable)} is called.
     * This method allows to programatically add code, e.g. for adding standard
     * environment, mocking non-modeled parts for model-checking.
     *
     * @throws IllegalStateException if the code is appended after the bprogram
     * started.
     * @param source JavaScript source to be added at the end of the current source.
     */
    public void appendSource(String source) {
        if (started) {
            throw new IllegalStateException("Cannot append code after the program had started.");
        } else {
            if (appendedCode == null) {
                appendedCode = new ArrayList<>();
            }
            appendedCode.add(source);
        }
    }

    /**
     * Adds more source code to be evaluated <em>before</em>
     * {@link #setupProgramScope(org.mozilla.javascript.Scriptable)} is called.
     * This method allows to programmatically add code, e.g. for adding standard
     * environment, mocking non-modeled parts for model-checking.
     *
     * @throws IllegalStateException if the code is appended after the bprogram
     * started.
     * @param source JavaScript source to be added at the beginning of the current source.
     */
    public void prependSource(String source) {
        if (started) {
            throw new IllegalStateException("Cannot prepend code after the program had started.");
        } else {
            if (prependedCode == null) {
                prependedCode = new ArrayList<>();
            }
            prependedCode.add(source);
        }
    }

    /**
     * Reads and evaluates the code at the passed input stream. The stream is
     * read to its end, but is not closed.
     *
     * @param inStrm Input stream for reading the script to be evaluated.
     * @param scriptName for error reporting purposes.
     * @param curCtx The context to evaluate the script in.
     * @return Result of evaluating the code at {@code inStrm}.
     */
    protected Object evaluate(InputStream inStrm, String scriptName, Context curCtx) {
        InputStreamReader streamReader = new InputStreamReader(inStrm, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(streamReader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("error while reading javascript from stream", e);
        }
        String script = sb.toString();
        return evaluate(script, scriptName, curCtx);
    }

    /**
     * Runs the passed code in the passed scope.
     *
     * @param script Code to evaluate
     * @param scriptName For error reporting purposes.
     * @param curCtx The context to evaluate the script in.
     * @return Result of code evaluation.
     */
    protected Object evaluate(String script, String scriptName, Context curCtx) {
        try {
            return curCtx.evaluateString(getGlobalScope(), script, scriptName, 1, null);
        } catch (EcmaError rerr) {
            throw new BPjsCodeEvaluationException(rerr);

        } catch (WrappedException wrapped) {
            try {
                throw wrapped.getCause();
            } catch (BPjsException be ) {
                throw be;
            } catch ( IllegalStateException ise ) {
                throw ise;
            } catch ( Throwable generalException ) {
                throw new BPjsRuntimeException("(Wrapped) Exception evaluating BProgram code: " + generalException.getMessage(), generalException);
            }

        } catch (EvaluatorException evalExp) {
            throw new BPjsCodeEvaluationException(evalExp);
            
        } catch ( Exception exp ) {
            throw new BPjsRuntimeException("Error evaluating BProgram code: " + exp.getMessage(), exp);
        }
    }

    /**
     * Registers a BThread into the program. If the program started, the BThread
     * will take part in the current bstep.
     *
     * @param bt the BThread to be registered.
     */
    public void registerBThread(BThreadSyncSnapshot bt) {
        recentlyRegisteredBthreads.add(bt);
        addBThreadCallback.ifPresent(cb -> cb.bthreadAdded(this, bt));
    }
    
    void registerForkedChild( BThreadSyncSnapshot btss ) {
        // make the top-level scope be *this* program's programScope
        Scriptable pus = ScriptableUtils.getPenultiamteParent(btss.getScope());
        pus.setParentScope(getGlobalScope());
        
        addBThreadCallback.ifPresent(cb -> cb.bthreadAdded(this, btss));
    }
    
    /**
     * Adds an event to {@code this}' external event queue.
     *
     * @param e The event to add.
     */
    public void enqueueExternalEvent(BEvent e) {
        recentlyEnqueuedExternalEvents.add(e);
    }

    /**
     * Sets up the program scope and evaluates the program source.
     *
     * <em>This method can only be called once per instance.</em>
     *
     * @return a snapshot of the program, after source code was executed, and
     * before any registered b-threads have run.
     * @throws IllegalStateException for repeated calls.
     */
    public BProgramSyncSnapshot setup() {
        if (started) {
            throw new IllegalStateException("Program already set up.");
        }
        Set<BThreadSyncSnapshot> bthreads = drainRecentlyRegisteredBthreads();

        if (eventSelectionStrategy == null) {
            eventSelectionStrategy = new SimpleEventSelectionStrategy();
        }
        FailedAssertionViolation failedAssertion = null;
        try (Context ctx = BPjs.enterRhinoContext()) {
            initProgramScope();

            // evaluate code in order
            if (prependedCode != null) {
                prependedCode.forEach(s -> evaluate(s, "prependedCode", ctx));
                prependedCode = null;
            }
            setupProgramScope(programScope);
            if (appendedCode != null) {
                appendedCode.forEach(s -> evaluate(s, "appendedCode", ctx));
                appendedCode = null;
            }

        } catch (FailedAssertionException fae) {
            failedAssertion = new FailedAssertionViolation(fae.getMessage(), "---init_code");
        }

        started = true;
        return new BProgramSyncSnapshot(this, bthreads, getStore(), Collections.emptyList(), failedAssertion);
    }

    private void initProgramScope() {

        programScope = BPjs.makeBPjsSubScope();

        jsProxy = preSetLogger==null?
                  new BProgramJsProxy(this)
                  :new BProgramJsProxy(this, preSetLogger);
        if ( preSetLogLevel != null ) {
            jsProxy.log.setLevel(preSetLogLevel.name());
        }
        if ( preSetPrintStream != null ) {
            jsProxy.log.setLoggerPrintStream(preSetPrintStream);
        }
        programScope.put("bp", programScope, Context.javaToJS(jsProxy, programScope));

        initialScopeValues.entrySet().forEach(e -> putInGlobalScope(e.getKey(), e.getValue()));
        initialScopeValues = null;
    }
   

    /**
     * The BProgram should set up its scope here. Normally, this amounts to
     * loading the script with the BThreads.
     *
     * @param scope the scope to set up.
     */
    protected abstract void setupProgramScope(Scriptable scope);

    /**
     * Blocks until an external event is added. Then, if that event is not the
     * "stop daemon mode" one, returns the event. Otherwise, returns
     * {@code null}.
     *
     * @return The event, or {@code null} in case the daemon mode is turned off
     * during the wait.
     * @throws InterruptedException (blocking call, we have to do this)
     */
    public BEvent takeExternalEvent() throws InterruptedException {
        BEvent next = recentlyEnqueuedExternalEvents.take();

        if (next == NO_MORE_WAIT_EXTERNAL) {
            waitForExternalEvents = false;
            return null;
        } else {
            return next;
        }
    }

    /**
     * Sets whether this program waits for external events or not.
     *
     * When set to {@code false}, when no events are available for selection,
     * the program terminates.
     *
     * @param shouldWait {@code true} causes the system to wait for external events. {@code false} causes it to not wait.
     */
    public void setWaitForExternalEvents(boolean shouldWait) {
        if (waitForExternalEvents && !shouldWait) {
            waitForExternalEvents = false;
            recentlyEnqueuedExternalEvents.add(NO_MORE_WAIT_EXTERNAL);
        } else {
            waitForExternalEvents = shouldWait;
        }
    }

    /**
     * Returns {@code true} iff the program waits for external events. When
     * {@code true}, the program will not terminate when it has no event
     * available for selection. Rather, it will wait for an external event to be
     * enqueued into its external event queue.
     *
     * @return {@code true} if this BProgram waits for external events,
     * {@code false} otherwise.
     * @see #enqueueExternalEvent(il.ac.bgu.cs.bp.bpjs.model.BEvent) 
     */
    public boolean isWaitForExternalEvents() {
        return waitForExternalEvents;
    }

    /**
     * Returns the program's global scope.
     *
     * @return the global scope of the program.
     */
    public Scriptable getGlobalScope() {
        return programScope;
    }

    /**
     * Adds an object to the program's global scope. JS code can reference the
     * added object by {@code name}.
     *
     * @param name The name under which {@code object} will be available to the
     * JS code.
     * @param obj The object to be added to the program's scope.
     */
    public void putInGlobalScope(String name, Object obj) {
        if (getGlobalScope() == null) {
            initialScopeValues.put(name, obj);
        } else {
            try (Context cx = BPjs.enterRhinoContext() ) {
                getGlobalScope().put(name, getGlobalScope(), Context.javaToJS(obj, getGlobalScope()));
            }
        }
    }

    /**
     * Gets the object pointer by the passed name in the global scope.
     *
     * @param <T> Class of the returned object.
     * @param name Name of the object in the JS heap.
     * @param clazz Class of the returned object
     * @return The object pointer by the passed name in the JS heap, converted
     * to the passed class.
     */
    public <T> Optional<T> getFromGlobalScope(String name, Class<T> clazz) {
        if (getGlobalScope().has(name, getGlobalScope())) {
            return Optional.of((T) Context.jsToJava(getGlobalScope().get(name, getGlobalScope()), clazz));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Sets the name of the program
     *
     * @param name the new program's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the program's name
     */
    public String getName() {
        return name;
    }
    
    public Map<String,Object> getStore() {
        return initialStore;
    }
    
    Set<BThreadSyncSnapshot> drainRecentlyRegisteredBthreads() {
        Set<BThreadSyncSnapshot> out = new HashSet<>(recentlyRegisteredBthreads.size());
        recentlyRegisteredBthreads.drainTo(out);
        return out;
    }

    List<BEvent> drainEnqueuedExternalEvents() {
        List<BEvent> out = new ArrayList<>(recentlyEnqueuedExternalEvents.size());
        recentlyEnqueuedExternalEvents.drainTo(out);
        return out;
    }
    
    void addFork( ForkStatement fkStmt ) {
        recentlyAddedForks.add(fkStmt);
    }
    
    Set<ForkStatement> drainRecentlyAddedForks() {
        Set<ForkStatement> out = new HashSet<>();
        recentlyAddedForks.drainTo(out);
        return out;
    }
    
    public void setAddBThreadCallback(BProgramCallback anAddBThreadCallback) {
        addBThreadCallback = Optional.ofNullable(anAddBThreadCallback);
    }

    public EventSelectionStrategy getEventSelectionStrategy() {
        if (eventSelectionStrategy == null) {
            setEventSelectionStrategy(new SimpleEventSelectionStrategy());
        }
        return eventSelectionStrategy;
    }
    
    /**
     * Sets the event selection strategy to be used.
     * @param <T> Actual type of the strategy
     * @param anEventSelectionStrategy the strategy
     * @return the strategy (so it can be cleanly assigned to a variable).
     */
    public <T extends EventSelectionStrategy> T setEventSelectionStrategy(T anEventSelectionStrategy) {
        eventSelectionStrategy = anEventSelectionStrategy;
        return anEventSelectionStrategy;
    }
    
    /**
     * Set the log level - logging called below the set level are ignored.
     * @param aLevel The new logging level
     */
    public void setLogLevel( BpLog.LogLevel aLevel ) {
        if ( jsProxy != null ) {
            jsProxy.log.setLevel(aLevel.name());
        } else {
            preSetLogLevel = aLevel;
        }
    }

    /**
     * Set the log implementation - logging called below the set level are logged using @BpLog implementation.
     *
     * @param logger The new logger implementation to use.
     * @see BpLog
     */
    public void setLogger(BpLog logger){
        if( jsProxy == null ) {
            preSetLogger = logger;
        }
        else throw new IllegalStateException("Cannot set logger after b-program has initialized");
    }

    /**
     * Set the print stream used for logging.
     * 
     * @param printStream the new logging destination stream
     */
    public void setLoggerOutputStreamer(PrintStream printStream){
        if ( jsProxy != null ) {
            jsProxy.log.setLoggerPrintStream(printStream);
        } else {
            preSetPrintStream = printStream;
        }
    }

    public BpLog.LogLevel getLogLevel() {
        return (jsProxy != null ) 
            ? BpLog.LogLevel.valueOf(jsProxy.log.getLevel())
            : (preSetLogLevel!= null)? preSetLogLevel: BpLog.DEFAULT_LOG_LEVEL;
    }

    public StorageModificationStrategy getStorageModificationStrategy() {
        return storageModificationStrategy;
    }

    public void setStorageModificationStrategy(StorageModificationStrategy storageModificationStrategy) {
        this.storageModificationStrategy = storageModificationStrategy;
    }
    
    @Override
    public String toString() {
        return "[BProgram " + getName() + "]";
    }

}
