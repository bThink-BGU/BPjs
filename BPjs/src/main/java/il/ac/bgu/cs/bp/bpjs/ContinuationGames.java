/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventsets.JsEventSet;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

/**
 * Playing around with continuations.
 *
 * @author michael
 */
public class ContinuationGames {

    static class BPP extends BProgram implements java.io.Serializable {

        @Override
        protected void setupProgramScope(Scriptable scope) {
            evaluateBpCode(scope,
                    "bp.registerBThread( \"bt\", function(){\n"
                    + "   bp.log.info(\"started\");"
//                    + "   bsync({});\n"
                    + "   bsync({request: bp.Event(\"e\")});\n"
                    + "   var i=1;"
                    + "   i = i+1;"
                    + "   bp.log.info(i);"
                    + "});", "");
        }

        @Override
        public void mainEventLoop() throws InterruptedException {
            try {
                BThreadSyncSnapshot bt = bthreads.iterator().next();
                Object cnt = bt.getContinuation();
                Context.enter();
                try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); 
                     ScriptableOutputStream outs = new ScriptableOutputStream(bytes, bt.getScope())) {
                    outs.writeObject(cnt);
                }
                Context.exit();

                Context globalContext = ContextFactory.getGlobal().enterContext();
                globalContext.setOptimizationLevel(-1); // must use interpreter mode
                for (int i = 0; i < 10; i++) {
                    globalContext.resumeContinuation(cnt, globalScope, "");
                }
                Context.exit();

            } catch (Exception ex) {
                Logger.getLogger(ContinuationGames.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Context ctxt = Context.enter();
        ctxt.setOptimizationLevel(-1);
        ImporterTopLevel importer = new ImporterTopLevel(ctxt);
        Scriptable globalScope = ctxt.initStandardObjects(importer);
            
        Function f = (Function) ctxt.evaluateString(globalScope, "function(){ var i=1; i++;}", "", 0, null);
    
        BThreadSyncSnapshot bss = new BThreadSyncSnapshot("aName", f);
        bss.setBSyncStatement( BSyncStatement.make()
                .request( new BEvent("hello") )
                .waitFor( new JsEventSet("S", f) )
        );
        bss.setInterruptHandler(f);
        
        try ( ByteArrayOutputStream bytes = new ByteArrayOutputStream(); 
              ScriptableOutputStream outs = new ScriptableOutputStream(bytes, globalScope) ) {
            outs.writeObject(bss);
        }
        Context.exit();
        
        BProgram bpp = new BPP();
        bpp.start();
    }
}
