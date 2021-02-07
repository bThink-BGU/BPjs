/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.execution;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.JsEventSet;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
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
        BProgramRunner bpr = new BProgramRunner(new ResourceBProgram("JsEventSet.js"));
        bpr.addListener(new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = bpr.addListener( new InMemoryEventLoggingListener() );
        bpr.run();
        
        assertEquals(Arrays.asList(new BEvent("1stEvent"), new BEvent("2ndEvent")), eventLogger.getEvents() );
    }
    
    @Test
    public void testNullPredicate() throws InterruptedException, URISyntaxException {
        BProgramRunner sut = new BProgramRunner(new StringBProgram("var es=bp.EventSet('bad',null);"));
        
        final AtomicBoolean errorCalled = new AtomicBoolean();
        final AtomicBoolean errorMadeSense = new AtomicBoolean();
        
        sut.addListener( new BProgramRunnerListenerAdapter() {
            @Override
            public void error(BProgram bp, Exception ex) {
                errorCalled.set(true);
                System.out.println("null predicate" + ex.getMessage());
                errorMadeSense.set(ex.getMessage().toLowerCase().contains("be a function"));
            }
        });

        sut.run();
        
        assertTrue( errorCalled.get() );
        assertTrue( errorMadeSense.get() );
    }
    
    
    @Test
    public void testNonBooleanPredicate() throws InterruptedException, URISyntaxException {
        BProgramRunner sut = new BProgramRunner(new StringBProgram(
                  "var es=bp.EventSet('bad',function(e){return 1;});\n"
                + "bp.registerBThread('a',function(){\n"
                + "  bp.sync({request:bp.Event('X')});"
                + "});\n"
                + "bp.registerBThread('b',function(){\n"
                + "  bp.sync({waitFor:es});\n"
                + "});"
        ));
 
        final AtomicBoolean errorCalled = new AtomicBoolean();
        final AtomicBoolean errorMadeSense = new AtomicBoolean();
        
        sut.addListener( new BProgramRunnerListenerAdapter() {
            @Override
            public void error(BProgram bp, Exception ex) {
                errorCalled.set(true);
                System.out.println("Bad predicate:" + ex.getMessage());
                errorMadeSense.set(ex.getMessage().toLowerCase().contains("boolean"));
            }
        });

        sut.run();
        
        assertTrue( errorCalled.get() );
        assertTrue( errorMadeSense.get() );
    }
    
    @Test
    public void testPredicateNullResult() throws InterruptedException, URISyntaxException {
        BProgramRunner sut = new BProgramRunner(new StringBProgram(
                  "var es=bp.EventSet('bad',function(e){return null;});\n"
                + "bp.registerBThread('a',function(){\n"
                + "  bp.sync({request:bp.Event('X')});"
                + "});\n"
                + "bp.registerBThread('b',function(){\n"
                + "  bp.sync({waitFor:es});\n"
                + "});"
        ));
 
        final AtomicBoolean errorCalled = new AtomicBoolean();
        final AtomicBoolean errorMadeSense = new AtomicBoolean();
        
        sut.addListener( new BProgramRunnerListenerAdapter() {
            @Override
            public void error(BProgram bp, Exception ex) {
                errorCalled.set(true);
                System.out.println("Bad predicate:" + ex.getMessage());
                errorMadeSense.set(ex.getMessage().toLowerCase().contains("null"));
            }
        });

        sut.run();
        
        assertTrue( errorCalled.get() );
        assertTrue( errorMadeSense.get() );
    }
    
    @Test
    public void testPredicateCrash() throws InterruptedException, URISyntaxException {
        BProgramRunner sut = new BProgramRunner(new StringBProgram(
                  "var es=bp.EventSet('bad',function(e){return a.b.c();});\n"
                + "bp.registerBThread('a',function(){\n"
                + "  bp.sync({request:bp.Event('X')});"
                + "});\n"
                + "bp.registerBThread('b',function(){\n"
                + "  bp.sync({waitFor:es});\n"
                + "});"
        ));
 
        final AtomicBoolean errorCalled = new AtomicBoolean();
        final AtomicBoolean errorMadeSense = new AtomicBoolean();
        
        sut.addListener( new BProgramRunnerListenerAdapter() {
            @Override
            public void error(BProgram bp, Exception ex) {
                errorCalled.set(true);
                System.out.println("Bad predicate:" + ex.getMessage());
                errorMadeSense.set(ex.getMessage().toLowerCase().contains("evaluating js predicate"));
            }
        });

        sut.run();
        
        assertTrue( errorCalled.get() );
        assertTrue( errorMadeSense.get() );
    }
    
    @Test
    public void testJsSetData() throws InterruptedException, URISyntaxException {
        try {
            BPjs.enterRhinoContext();
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
