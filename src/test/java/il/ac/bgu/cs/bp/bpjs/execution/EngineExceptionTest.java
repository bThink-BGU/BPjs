package il.ac.bgu.cs.bp.bpjs.execution;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsCodeEvaluationException;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
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
        BProgram sut = new StringBProgram(
            "bad",
            "var i=0;\n var j=42;\n var k=5; bp.sync({request:bp.Event(\"A\")});"
        );
        
        try { 
            new BProgramRunner(sut).run();
            fail("System should have thrown an error due to bp.sync called outside of a BThread.");
        } catch (BPjsCodeEvaluationException exp) {
            assertTrue( exp.getMessage().contains("bp.sync"));
            System.out.println("Error message details: ");
            System.out.println(exp.getDetails());
        }
    }
    
    @Test
    public void testInvalidJavascript() throws InterruptedException {
        BProgram sut = new BProgram("bad"){
            @Override
            protected void setupProgramScope(Scriptable scope) {
                evaluate("var j=9\n"
                        + "#This isn't a javascript line.\n"
                        + "var o=0;",
                        "hardcoded");
            }
        };
        
        try { 
            new BProgramRunner(sut).run();
            fail("System should have thrown an error due to uncompilable Javascript code.");
        } catch (BPjsCodeEvaluationException exp) {
            assertEquals( 2, exp.getLineNumber() );
            assertEquals( 1, exp.getColumnNumber() );
            assertEquals( "hardcoded", exp.getSourceName() );
            assertEquals("#This isn't a javascript line.", exp.getLineSource());
            
        }
    }
}
