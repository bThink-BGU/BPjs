/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSets;
import il.ac.bgu.cs.bp.bpjs.eventsets.JsEventSet;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.mozilla.javascript.Function;

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
    
    public enum LogLevel {Off, Warn, Info, Fine}
    
    public static class BpLog implements java.io.Serializable {
        LogLevel level = LogLevel.Info;
        
        public void warn( String msg ) { log( LogLevel.Warn, msg ); }
        public void info( String msg ) { log( LogLevel.Info, msg ); }
        public void fine( String msg ) { log( LogLevel.Fine, msg ); }
        
        public void log( LogLevel lvl, String msg ) {
            if ( level.compareTo(lvl) >= 0) {
                System.out.println("[JS][" + lvl.name() + "] " + msg );
            }
        }
        
        public void setLevel( String levelName ) {
            synchronized(this){
                level = LogLevel.valueOf(levelName);
            }
        }
        
        public String getLevel() {
            return level.name();
        }
    }
    
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
        program.setDaemonMode( newDaemonMode );
    }
    
    public boolean isDaemonMode() {
        return program.isDaemonMode();
    }
    
    /**
     * Loads a Javascript resource (a file that's included in the .jar).
     *
     * @param path absolute path of the resource in the .jar file.
     */
    public void loadJavascriptResource(String path) {
        program.evaluateResource(path);
    }
    
    
}
