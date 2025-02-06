package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeFunction;

/**
 * An event set whose predicate is a JavaScript function.
 *
 * @author michael
 */
public class JsEventSet implements EventSet, java.io.Serializable {

    private final Function predicate;
    private final String name;
    private final String rawSource;

    public JsEventSet(String aName, Function aPredicate) {
        name = aName;
        predicate = aPredicate;
        if ( aPredicate instanceof NativeFunction ) {
//            rawSource = ((NativeFunction)aPredicate).getEncodedSource();
            rawSource = ((NativeFunction)aPredicate).getRawSource(); // for Rhino 1.8.0
        } else {
            rawSource = null;
        }
    }

    @Override
    public boolean contains(BEvent event) {
        try {
            Object result = predicate.call(Context.getCurrentContext(), predicate, predicate.getParentScope(),
                    new Object[]{Context.javaToJS(event, predicate.getParentScope())});

            Boolean res = (Boolean) Context.jsToJava(result, Boolean.class);
            if ( res == null ) {
                throw new BPjsRuntimeException("EventSet " + name + ": JS Predicate returned null, not a boolean value. " + predicate.toString());
            }
            return res;
        } catch (EvaluatorException ee) {
            throw new BPjsRuntimeException("EventSet " + name + ": JS Predicate did not return a boolean value.", ee);
        } catch (EcmaError ee) {
            throw new BPjsRuntimeException("Error evaluating JS Predicate " + name + ": " + ee.getMessage(), ee);
        }
    }

    public Function getPredicate() {
        return predicate;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "[JsEventSet: " + getName() +"]";
    }
    
    @Override
    public boolean equals( Object other ) {
        if ( other == null ) return false;
        if ( other == this ) return true;
        
        if ( other instanceof JsEventSet ) {
            JsEventSet otherES = (JsEventSet) other;
            if ( !name.equals(otherES.name) ) {
                return false;
            } else if ( rawSource != null ) {
                return rawSource.equals(otherES.rawSource);
            } else {
                return predicate.equals(((JsEventSet) other).predicate);
            }
                
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return (rawSource!=null) ? rawSource.hashCode() : predicate.hashCode();
    }
}
