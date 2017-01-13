package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine;

import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsCodeEvaluationException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author michael
 */
public class EngineExceptionTest {
    @Test
    public void testInvalidBSyncCall() throws InterruptedException {
        BProgram sut = new BProgram("bad"){
            @Override
            protected void setupProgramScope(Scriptable scope) {
                evaluate("var i=0;\n var j=42;\n var k=5; bsync({request:bp.Event(\"A\")});", "hardcoded");
            }
        };
        
        try { 
            sut.start();
            fail("System should have thrown an error due to bsync called outside of a BThread.");
        } catch (BPjsCodeEvaluationException exp) {
            assertEquals( 3, exp.getLineNumber() );
            assertTrue( exp.getMessage().contains("bsync"));
            assertTrue( exp.getMessage().contains("Did you forget")); // make sure this is the friendly message
            assertEquals(1, exp.getScriptStackTrace().size());
        }
    }
    
    @Test
    public void testInvalidJavascript() throws InterruptedException {
        BProgram sut = new BProgram("bad"){
            @Override
            protected void setupProgramScope(Scriptable scope) {
                evaluate("var j=9\n"
                        + "I'm not a Javascript code at all.\n"
                        + "var o=0;",
                        "hardcoded");
            }
        };
        
        try { 
            sut.start();
            fail("System should have thrown an error due to uncompilable Javascript code.");
        } catch (BPjsCodeEvaluationException exp) {
            assertEquals( 2, exp.getLineNumber() );
        }
    }
}
