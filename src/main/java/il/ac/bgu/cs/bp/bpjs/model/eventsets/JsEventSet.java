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
        // Capture a source form that stays comparable across Rhino versions and
        // recreated function objects.
        rawSource = extractStableSource(aPredicate);
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

    private static String extractStableSource(Function predicate) {
        if (predicate instanceof NativeFunction) {
            // Raw source is the most stable representation when Rhino exposes it directly.
            String source = ((NativeFunction) predicate).getRawSource();
            if (source != null && !source.isEmpty()) {
                return source;
            }
        }

        Context ctx = Context.getCurrentContext();
        if (ctx != null) {
            try {
                // Decompile when raw source is unavailable, such as for newer function forms.
                String source = ctx.decompileFunction(predicate, 0);
                if (source != null && !source.isBlank()) {
                    return source;
                }
            } catch (RuntimeException ignored) {
                // Fall back to the function's string representation if decompilation is unavailable.
            }
        }

        return predicate.toString();
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
            } else if ( rawSource != null && otherES.rawSource != null ) {
                // Prefer source-based equality so equivalent predicates still match
                // after being recreated during snapshotting or restoration.
                return rawSource.equals(otherES.rawSource);
            } else {
                if (predicate.equals(((JsEventSet) other).predicate)) return true;
                return predicate.toString().equals(((JsEventSet) other).predicate.toString());
            }
                
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        // Keep the hash aligned with equals by using the same stable identity:
        // the event-set name plus the extracted predicate source.
        return java.util.Objects.hash(name, rawSource);
    }
}
