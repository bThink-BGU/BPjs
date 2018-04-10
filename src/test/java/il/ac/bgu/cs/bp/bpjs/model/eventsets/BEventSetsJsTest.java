/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs.model.eventsets;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


public class BEventSetsJsTest {
    
    BProgram prog;
    Map<String, BEvent> events = new HashMap<>();
    Map<String, EventSet> eventSets = new HashMap<>();
    
    @Before
    public void setUp() throws InterruptedException {
        prog = new BProgram() {
            @Override
            protected void setupProgramScope(Scriptable aScope) {
                aScope.put("events", aScope, Context.javaToJS(events, aScope));
                aScope.put("eventSets", aScope, Context.javaToJS(eventSets, aScope));
                aScope.put("test", aScope, Context.javaToJS(BEventSetsJsTest.this, aScope));
                evaluate("eventSets.put(\"esName\", bp.EventSet( 'x', function(e){ "
                                + "bp.log.info('esName');\n"
                                + "bp.log.info(e);\n"
                                + "bp.log.info(e.name);\n"
                                + "bp.log.info( e.name==='Name' );\n"
                                + "return e.name=='Name'; }) );\n" +
                        "eventSets.put(\"esDataObjVizIsViz\", bp.EventSet( 'x',  function(e){ "
                                + "bp.log.info('esDataObjVizIsViz');\n"
                                + "bp.log.info(e);\n"
                                + "return (e.data) ? e.data.viz=='viz' : false; }) );\n" +
                        "eventSets.put(\"esDataIsViz\", bp.EventSet( 'x',  function(e){ "
                                + "bp.log.info('esDataIsViz');\n"
                                + "bp.log.info(e);\n"
                                + "bp.log.info(e.data);\n"
                                + "return e.data==\"viz\"; }) );\n" +
                                
                        "events.put( \"eName\", bp.Event(\"Name\") );\n" +
                        "events.put( \"eNotName\", bp.Event(\"NotName\") );\n" +
                        "events.put( \"eVizObj\", bp.Event(\"aName\", {viz:\"viz\"}) );\n" +
                        "events.put( \"eViz\", bp.Event('aName', 'viz') );"
                        ,
                        "inline script" );
            }
        };
        new BProgramRunner(prog).run();
    }
    

    @Test
    public void testContains_Name() throws InterruptedException {
        BEvent eName = events.get("eName");
        BEvent eNotName = events.get("eNotName");
        EventSet esName = eventSets.get("esName");
        
        Context.enter();
        assertTrue( esName.contains(eName) );
        assertFalse( esName.contains(eNotName) );
        Context.exit();
    }
    
    @Test
    public void testContains_Data() throws Exception {
        BEvent eVizObj = events.get("eVizObj");
        BEvent eViz = events.get("eViz");
        BEvent eName = events.get("eName");
        EventSet esDataVizIsViz = eventSets.get("esDataObjVizIsViz");
        EventSet esDataIsViz = eventSets.get("esDataIsViz");
        Context.enter();
        assertTrue( esDataVizIsViz.contains(eVizObj) );
        assertTrue( esDataIsViz.contains(eViz) );
        
        assertFalse( esDataIsViz.contains(eVizObj) );
        assertFalse( esDataVizIsViz.contains(eViz) );
        assertFalse( esDataVizIsViz.contains(eName) );
        assertFalse( esDataIsViz.contains(eName) );
        Context.exit();
    }
   
    
    public void print(Object o) {
        System.out.println("From JS: " + o);
    }
}
