package il.ac.bgu.cs.bp.bpjs.exceptions;

import java.util.Arrays;
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
    
    public List<String> getScriptStackTrace() {
        return Arrays.stream(cause.getScriptStack())
                .map(sf->String.format("%s, %s:%d", sf.fileName, (sf.functionName!=null)? sf.functionName : "<top scope>", sf.lineNumber) )
                .collect( Collectors.toList() );
    }
    
    public String getDetails() {
        return cause.details();
    }

    public final String getSourceName() {
        return cause.sourceName();
    }

    public final int getLineNumber() {
        return cause.lineNumber();
    }

    public final int getColumnNumber() {
        return cause.columnNumber();
    }

    public final String getLineSource() {
        return cause.lineSource();
    }
   
}