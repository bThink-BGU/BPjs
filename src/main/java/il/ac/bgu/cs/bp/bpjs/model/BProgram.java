package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.util.*;
import java.util.concurrent.*;

import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsCodeEvaluationException;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsException;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.FailedAssertionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import java.util.concurrent.atomic.AtomicInteger;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.WrappedException;

/**
 * Base class for BPrograms. Provides the context (Javascript scope, external 
 * event queue, etc.) bthreads interact with while running.
 * Concrete BProgram extend this class by implementing 
 * the {@link #setupProgramScope(org.mozilla.javascript.Scriptable)} method.
 * 
 * <p>
 * For creating a BProgram that uses a single Javascript file available in the
 * classpath, see {@link SingleResourceBProgram}. For creating them from a 
 * hard-coded string, see {@link StringBProgram}.
 *
 * @author michael
 */
public abstract class BProgram {
    
    // ------------- Static Members ---------------
    
    /**
     * "Poison pill" to insert to the external event queue. Used only to turn the
     * daemon mode off.
     */
    static final BEvent NO_MORE_DAEMON = new BEvent("NO_MORE_DAEMON");
    
    /** Counter for giving anonymous instances some semantic name. */
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

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
     * internal ones are available.
     */
    private boolean daemonMode;

    /**
     * Events are enqueued here by external threads
     */
    private final BlockingQueue<BEvent> recentlyEnqueuedExternalEvents = new LinkedBlockingQueue<>();

    /**
     * BThreads added between bsyncs are added here.
     */
    private final BlockingQueue<BThreadSyncSnapshot> recentlyRegisteredBthreads = new LinkedBlockingDeque<>();

    private volatile boolean started = false;

    protected Scriptable programScope;
    
    private EventSelectionStrategy eventSelectionStrategy;
    
    /**
     * Objects that client code wishes to put in scope before the scope is 
     * initialized are collected here.
     */
    protected Map<String,Object> initialScopeValues = new HashMap<>();
    
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
     * @param aName name for the new BProgram.
     */
    public BProgram(String aName) {
        name = aName;
    }
    
    /**
     * Creates a BProgram with a specific name and an event selection strategy.
     * @param aName Name for the program.
     * @param anEss Event selection strategy.
     */
    public BProgram( String aName, EventSelectionStrategy anEss ) {
        name = aName;
        eventSelectionStrategy = anEss;
    }

    /**
     * Loads a Javascript resource (a file that's included in the .jar).
     *
     * @param pathInJar path of the resource, relative to the class.
     */
    public void evaluateResource(String pathInJar) {
        try ( InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathInJar) ) {
            if (resource == null) {
                throw new RuntimeException("Resource '" + pathInJar + "' not found.");
            }
            evaluate(resource, pathInJar);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading resource: '" + pathInJar +"': " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Adds more source code to be evaluated <em>after</em> {@link #setupProgramScope(org.mozilla.javascript.Scriptable)}
     * is called. This method allows to programatically add code, e.g. for adding standard environment, mocking non-modeled
     * parts for model-checking.
     * 
     * @throws IllegalStateException if the code is appended after the bprogram started.
     * @param source Valid JS source code
     */
    public void appendSource( String source ) {
        if ( started ) {
            throw new IllegalStateException("Cannot append code after the program had started.");
        } else {
            if ( appendedCode == null ) {
                appendedCode = new ArrayList<>();
            }
            appendedCode.add(source);
        }
    }
    
    /**
     * Adds more source code to be evaluated <em>before</em> {@link #setupProgramScope(org.mozilla.javascript.Scriptable)}
     * is called. This method allows to programatically add code, e.g. for adding standard environment, mocking non-modeled
     * parts for model-checking.
     * 
     * @throws IllegalStateException if the code is appended after the bprogram started.
     * @param source Valid JS source code
     */
    public void prependSource( String source ) {
        if ( started ) {
            throw new IllegalStateException("Cannot append code after the program had started.");
        } else {
            if ( prependedCode == null ) {
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
     * @return Result of evaluating the code at {@code inStrm}.
     */
    protected Object evaluate(InputStream inStrm, String scriptName) {
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
        return evaluate(script, scriptName);
    }

    /**
     * Runs the passed code in the passed scope.
     * @param script Code to evaluate
     * @param scriptName For error reporting purposes.
     * @return Result of code evaluation.
     */
    protected Object evaluate(String script, String scriptName) {
        try {
            Context curCtx = Context.getCurrentContext();
            curCtx.setLanguageVersion(170);
            return curCtx.evaluateString(programScope, script, scriptName, 1, null);
        } catch (EcmaError rerr) {
            if ( rerr.getErrorMessage().trim().equals("\"bsync\" is not defined.") ) {
                throw new BPjsCodeEvaluationException("'bsync' is only defined in BThreads. Did you forget to call 'bp.registerBThread()'?", rerr);
            }
            throw new BPjsCodeEvaluationException(rerr);
            
        } catch (WrappedException wrapped) {
            if ( wrapped.getCause() instanceof BPjsException ) {
                throw (BPjsException)wrapped.getCause();
            } else {
                throw wrapped;
            }
            
        } catch (EvaluatorException evalExp) {
            throw new BPjsCodeEvaluationException(evalExp);
        }
    }
    
    /**
     * Registers a BThread into the program. If the program started, the BThread
     * will take part in the current bstep.
     *
     * @param bt the BThread to be registered.
     */
    public void registerBThread(BThreadSyncSnapshot bt) {
        bt.setupScope(programScope);
        recentlyRegisteredBthreads.add(bt);
        addBThreadCallback.ifPresent( cb -> cb.bthreadAdded(this, bt));
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
     *         before any registered b-threads have run.
     * @throws IllegalStateException for repeated calls.
     */
    public BProgramSyncSnapshot setup() {
        if ( started ) { 
            throw new IllegalStateException("Program already set up.");
        }
        Set<BThreadSyncSnapshot> bthreads = drainRecentlyRegisteredBthreads();
        
        if ( eventSelectionStrategy==null ) {
            eventSelectionStrategy = new SimpleEventSelectionStrategy();
        }
        FailedAssertion failedAssertion = null;
        try {
            Context cx = ContextFactory.getGlobal().enterContext();
            cx.setOptimizationLevel(-1); // must use interpreter mode
            initProgramScope(cx);
            if ( prependedCode != null ) {
                prependedCode.forEach( s -> evaluate(s, "prependedCode") );
            }
            setupProgramScope(programScope);
            bthreads.forEach(bt -> bt.setupScope(programScope));
            if ( appendedCode != null ) {
                appendedCode.forEach( s -> evaluate(s, "appendedCode") );
            }
            
        } catch ( FailedAssertionException fae ) {
            failedAssertion = new FailedAssertion(fae.getMessage(), "---init_code");
            
        } finally {
            Context.exit();
        }
        
        started = true;
        return new BProgramSyncSnapshot(this, bthreads, Collections.emptyList(), failedAssertion);
    }

    private void initProgramScope(Context cx) {
        // load and execute globalScopeInit.js
        ImporterTopLevel importer = new ImporterTopLevel(cx);
        programScope = cx.initStandardObjects(importer);
        BProgramJsProxy proxy = new BProgramJsProxy(this);
        programScope.put("bp", programScope,
        Context.javaToJS(proxy, programScope));
        
//        evaluateResource("globalScopeInit.js");// <-- Currently not needed. Leaving in as we might need it soon.
        initialScopeValues.forEach((key, value) -> putInGlobalScope(key, value));
        initialScopeValues=null;
    }

    /**
     * The BProgram should set up its scope here. Normally, this amounts to 
     * loading the script with the BThreads.
     *
     * @param scope the scope to set up.
     */
    protected abstract void setupProgramScope(Scriptable scope);

    /**
     * Blocks until an external event is added. Then, if that event
     * is not the "stop daemon mode" one, returns the event. Otherwise,
     * returns {@code null}.
     * @return The event, or {@code null} in case the daemon mode
     *         is turned off during the wait.
     * @throws InterruptedException 
     */
    public BEvent takeExternalEvent() throws InterruptedException {
        BEvent next = recentlyEnqueuedExternalEvents.take();
        
        if (next == NO_MORE_DAEMON) {
            daemonMode = false;
            return null;
        } else {
            return next;
        }
    }

    /**
     * Sets whether this program is a daemon or not. When daemon, program will
     * wait for external events even when there are no selectable internal events. 
     *
     * In normal mode ({@code daemon==false}), when no events are available for 
     * selection, the program terminates.
     * 
     * @param newDaemonMode {@code true} to make the program a daemon, 
     *                      {@code false} otherwise.
     */
    public void setDaemonMode(boolean newDaemonMode) {
        if (daemonMode && !newDaemonMode) {
            daemonMode = false;
            recentlyEnqueuedExternalEvents.add(NO_MORE_DAEMON);
        } else {
            daemonMode = newDaemonMode;
        }
    }

    /**
     * Returns {@code true} iff the program is in daemon mode. When in this mode,
     * the program will not terminate when it has no event available for selection.
     * Rather, it will wait for an external event to be enqueued into its external
     * event queue.
     * 
     * @return {@code true} if this BProgram is in daemon mode,
     *         {@code false} otherwise.
     * @see #enqueueExternalEvent(il.ac.bgu.cs.bp.bpjs.events.BEvent) 
     */
    public boolean isDaemonMode() {
        return daemonMode;
    }
    
    /**
     * Returns the program's global scope.
     * @return the global scope of the program.
     */
    public Scriptable getGlobalScope() {
        return programScope;
    }
    
    /**
     * Adds an object to the program's global scope. JS code can reference the 
     * added object by {@code name}.
     * @param name The name under which {@code object} will be available to the JS code.
     * @param obj The object to be added to the program's scope.
     */
    public void putInGlobalScope( String name, Object obj ) {
        if ( getGlobalScope() == null ) {
            initialScopeValues.put(name, obj);
        } else {
            try {
                Context.enter();
                getGlobalScope().put(name, programScope, Context.javaToJS(obj, programScope));
            } finally {
                Context.exit();
            }
        }
    }
    
    /**
     * Gets the object pointer by the passed name in the global scope.
     * @param <T> Class of the returned object.
     * @param name Name of the object in the JS heap.
     * @param clazz Class of the returned object
     * @return The object pointer by the passed name in the JS heap, converted to
     *         the passed class.
     */
    public <T> Optional<T> getFromGlobalScope( String name, Class<T> clazz ) {
        if ( getGlobalScope().has(name, programScope) ) {
            return Optional.of( (T)Context.jsToJava(getGlobalScope().get(name, getGlobalScope()), clazz) );
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Sets the name of the program
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
    
    Set<BThreadSyncSnapshot> drainRecentlyRegisteredBthreads() {
        Set<BThreadSyncSnapshot> out = new HashSet<>();
        recentlyRegisteredBthreads.drainTo(out);
        return out;
    }
    
    List<BEvent> drainEnqueuedExternalEvents() {
        List<BEvent> out = new ArrayList<>(recentlyEnqueuedExternalEvents.size());
        recentlyEnqueuedExternalEvents.drainTo(out);
        return out;
    }

    public void setAddBThreadCallback(BProgramCallback anAddBThreadCallback) {
        addBThreadCallback = Optional.ofNullable(anAddBThreadCallback);
    }

    public EventSelectionStrategy getEventSelectionStrategy() {
        if ( eventSelectionStrategy == null ) {
            setEventSelectionStrategy( new SimpleEventSelectionStrategy() );
        }
        return eventSelectionStrategy;
    }

    public void setEventSelectionStrategy(EventSelectionStrategy eventSelectionStrategy) {
        this.eventSelectionStrategy = eventSelectionStrategy;
    }
    
    @Override
    public String toString() {
        return "[BProgram " + getName() + "]";
    }
    
}
