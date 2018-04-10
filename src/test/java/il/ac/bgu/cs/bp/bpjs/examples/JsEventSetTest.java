/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.JsEventSet;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import java.net.URISyntaxException;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;

/**
 *
 * @author michael
 */
public class JsEventSetTest {
    
    @Test
    public void testRun() throws InterruptedException, URISyntaxException {
        BProgramRunner bpr = new BProgramRunner(new SingleResourceBProgram("JsEventSet.js"));
        bpr.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = bpr.addListener( new InMemoryEventLoggingListener() );
        bpr.run();
        
        assertEquals(Arrays.asList(new BEvent("1stEvent"), new BEvent("2ndEvent")), eventLogger.getEvents() );
    }
    
    @Test(expected=BPjsRuntimeException.class)
    public void testNullPredicate() throws InterruptedException, URISyntaxException {
        new BProgramRunner(new StringBProgram("var es=bp.EventSet('bad',null);")).run();
    }
    
    
    @Test(expected=BPjsRuntimeException.class)
    public void testBadPredicate() throws InterruptedException, URISyntaxException {
        new BProgramRunner(new StringBProgram(
                  "var es=bp.EventSet('bad',function(e){return 1;});\n"
                + "bp.registerBThread('a',function(){\n"
                + "  bsync({request:bp.Event('X')});"
                + "});\n"
                + "bp.registerBThread('b',function(){\n"
                + "  bsync({waitFor:es});\n"
                + "});"
        )).run();
    }
    
    @Test
    public void testJsSetData() throws InterruptedException, URISyntaxException {
        try {
            Context.enter();
            BProgram bpr = new StringBProgram( "Eventset",
                      "var es=bp.EventSet('a',function(e){return e.name=='a';});\n"
            );
            new BProgramRunner(bpr).run();
            NativeJavaObject sut = (NativeJavaObject) bpr.getGlobalScope().get("es", bpr.getGlobalScope());
            JsEventSet jsSut = (JsEventSet) Context.jsToJava(sut, JsEventSet.class);
            assertEquals("a", jsSut.getName());
            assertTrue( jsSut.toString().contains("a"));
            assertTrue( jsSut.toString().contains("JsEventSet"));
            
            assertTrue( jsSut.contains(BEvent.named("a")) );
            assertFalse( jsSut.contains(BEvent.named("b")) );
            
        } finally {
            Context.exit();
        }
        
        
    }
    
}
