package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import org.mozilla.javascript.Scriptable;

/**
 * A convenience class for making a BProgram from a String.
 * 
 * @author michael
 */
public class StringBProgram extends BProgram {
    
    private final String sourceCode;

    public StringBProgram(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public StringBProgram(String sourceCode, String aName) {
        super(aName);
        this.sourceCode = sourceCode;
    }
    
    @Override
    protected void setupProgramScope(Scriptable scope) {
        evaluate(sourceCode, "StringBProgram");
    }
    
}
