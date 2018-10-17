package il.ac.bgu.cs.bp.bpjs.exceptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.RhinoException;

/**
 * Thrown when Javascript code fails to evaluate.
 * 
 * @author michael
 */
public class BPjsCodeEvaluationException extends BPjsException {
   
    private final RhinoException cause;

    public BPjsCodeEvaluationException(EcmaError cause) {
        super(cause.details() + " (at " + cause.lineNumber() + ":" + cause.columnNumber() + ")", cause);
        this.cause = cause;
    }
    
    public BPjsCodeEvaluationException(String message, EcmaError cause) {
        super(message, cause);
        this.cause = cause;
    }

    public BPjsCodeEvaluationException(EvaluatorException cause) {
        super(cause.details() + " (at " + cause.lineNumber() + ":" + cause.columnNumber() + ")", cause);
        this.cause = cause;
    }

    public BPjsCodeEvaluationException(String message) {
        super(message);
        cause = null;
    }
    
    public List<String> getScriptStackTrace() {
        if ( cause == null ) {
            return Collections.emptyList();
        } else {
            return Arrays.stream(cause.getScriptStack())
                    .map(sf->String.format("%s, %s:%d", sf.fileName, (sf.functionName!=null)? sf.functionName : "<top scope>", sf.lineNumber) )
                    .collect( Collectors.toList() );
        }
    }
    
    public String getDetails() {
        return (cause!=null) ? cause.details() : null;
    }

    public final String getSourceName() {
        return (cause!=null) ? cause.sourceName() : null;
    }

    public final int getLineNumber() {
        return  (cause!=null) ?cause.lineNumber() : -1;
    }

    public final int getColumnNumber() {
        return (cause!=null) ?cause.columnNumber() : -1;
    }

    public final String getLineSource() {
        return (cause!=null) ?cause.lineSource() : null;
    }
   
}