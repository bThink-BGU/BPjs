/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import java.util.Objects;
import org.mozilla.javascript.Function;

/**
 * Serves as {@code this} (of sorts) for Javascript BThread code. Public methods
 * in this class are directly callable form BThread code, no qualification needed.
 * 
 * @author michael
 */
public class BThreadJsProxy implements java.io.Serializable {
    
    private BThreadSyncSnapshot bthread;

    public BThreadJsProxy(BThreadSyncSnapshot aBthread) {
        bthread = aBthread;
    }
    
    public BThreadJsProxy() {}

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
