/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
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
 * Serves as {@code this} (of sorts) for Javascript BThread code. Public methods
 * in this class are directly callable form BThread code, no qualification needed.
 * 
 * For example, the following code invokes {@link #bsync(org.mozilla.javascript.NativeObject) }:
 * 
 * <pre><code>
 * bp.registerBThread( function(){
 *   bsync(...);
 * });
 * </code></pre>
 * @author michael
 */
public class BThreadJsProxy implements java.io.Serializable {
    
    private static volatile boolean deprecationWarningPrinted = false;
    
    private BThreadSyncSnapshot bthread;

    public BThreadJsProxy(BThreadSyncSnapshot aBthread) {
        bthread = aBthread;
    }
    
    public BThreadJsProxy() {}
    
    @Deprecated
    public void bsync( NativeObject jsRWB ) {
        bsync(jsRWB, null);
    }
    
    @Deprecated
    public void bsync( NativeObject jsRWB, Object data ) {
        if ( ! deprecationWarningPrinted ) {
            deprecationWarningPrinted = true;
            System.err.println("Warning: bsync is deprecated and will be removed shortly. Please use bp.sync instead.");
        }
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
        
        stmt = stmt.waitFor( convertToEventSet(jRWB.get("waitFor")) )
                     .block( convertToEventSet(jRWB.get("block")) )
                 .interrupt( convertToEventSet(jRWB.get("interrupt")) )
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
        bthread.setBSyncStatement(stmt);
        stmt.setBthread(bthread);
        ContinuationPending capturedContinuation = Context.getCurrentContext().captureContinuation();
        capturedContinuation.setApplicationState(stmt);
        throw capturedContinuation;
    }

    public void setInterruptHandler( Object aPossibleHandler ) {
        bthread.setInterruptHandler(
                (aPossibleHandler instanceof Function) ? (Function) aPossibleHandler: null );
    }
    
    public void setBThread(BThreadSyncSnapshot bthread) {
        this.bthread = bthread;
    }

    public BThreadSyncSnapshot getBThread() {
        return bthread;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.bthread);
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
        final BThreadJsProxy other = (BThreadJsProxy) obj;
        return Objects.equals(this.bthread, other.bthread);
    }
    
    
}
