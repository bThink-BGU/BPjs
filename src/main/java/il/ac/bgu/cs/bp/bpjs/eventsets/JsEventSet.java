/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.eventsets;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;

/**
 * An event set whose predicate is a Javascript function.
 *
 * @author michael
 */
public class JsEventSet implements EventSet, java.io.Serializable {

    private final Function predicate;
    private final String name;

    public JsEventSet(String aName, Function aPredicate) {
        name = aName;
        predicate = aPredicate;
    }

    @Override
    public boolean contains(BEvent event) {
        try {
            Object result = predicate.call(Context.getCurrentContext(), predicate, predicate.getParentScope(),
                    new Object[]{Context.javaToJS(event, predicate.getParentScope())});

            Boolean res = (Boolean) Context.jsToJava(result, Boolean.class);
            if (res == null) {
                throw new RuntimeException("JS Predicate returned null, not a boolean value. " + predicate.toString());
            }
            return res;
        } catch (EvaluatorException ee) {
            throw new RuntimeException("JS Predicate did not return a boolean value.", ee);
        } catch (EcmaError ee) {
            throw new RuntimeException("Error evaluating JS Predicate:" + ee.getMessage(), ee);
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
        return "EventSet:" + getName();
    }
}
