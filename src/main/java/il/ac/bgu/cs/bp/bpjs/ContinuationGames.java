/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

/**
 * Playing around with continuations.
 *
 * TODO: The BPP class should be a BProgramRunner now.
 * 
 * @author michael
 */
public class ContinuationGames {

    static class BPP extends BProgram implements java.io.Serializable {

        @Override
        protected void setupProgramScope(Scriptable scope) {
            evaluate(" j=1;" +
                    "bp.registerBThread( \"bt\", function(){\n"
                    + "   bp.log.info(\"started\");"
//                    + "   bsync({});\n"
                    + "   var i=1;"
                    + "   bsync({request: bp.Event(\"e\")});\n"
                    + "   bp.log.info('i:' + i + ' j:'+j);"
                    + "   i = i+1;"
                    + "   j = j+1;"
                    + "});", "");
        }

        public void mainEventLoop() throws InterruptedException {
            try {
                BThreadSyncSnapshot bt = bthreads.iterator().next();
                Context ctxt = Context.enter();
                Object cnt = bt.getContinuation();
                final Scriptable scope = bt.getScope();
                final Scriptable topLevelScope = ctxt.initSafeStandardObjects();
                
                Object bp = null;
                for ( Scriptable sc=scope; sc!=null; sc = sc.getParentScope() ) {
                    System.out.println("SCOPE START");
                    if ( sc.has("bp", sc) ) {
                        bp = sc.get("bp", sc);
                        sc.delete("bp");
                        System.out.println("bp deleted.");
                    }
                    System.out.println("SCOPE END\n\n");
                }
                
                
                byte[] serializedContinuationAndScope;
                try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); 
                     ScriptableOutputStream outs = new ScriptableOutputStream(bytes, topLevelScope)) {
                    outs.writeObject(cnt);
                    outs.writeObject(scope);
                    outs.flush();
                    serializedContinuationAndScope = bytes.toByteArray();
                }
                Context.exit();

                System.out.println("Seriazlied to " + serializedContinuationAndScope.length + " bytes.");
                Context cx = ContextFactory.getGlobal().enterContext();
                cx.setOptimizationLevel(-1); // must use interpreter mode
                
                for (int i = 0; i < 10; i++) {
                    try ( ScriptableInputStream sis = new ScriptableInputStream( new ByteArrayInputStream(serializedContinuationAndScope), topLevelScope)) {
                        // read cnt and scope
                        Scriptable cnt2 = (Scriptable) sis.readObject();
                        Scriptable scope2 = (Scriptable) sis.readObject();
                        
                        // re-add bp to the scope
                        initProgramScope(cx);
                        scope2.setParentScope(getGlobalScope());
                        
                        // go!!
                        cx.resumeContinuation(cnt2, scope2, new Object[0]);
                        cx.resumeContinuation(cnt2, scope2, new Object[0]);
                    }
                }
                Context.exit();
            } catch (Exception ex) {
                Logger.getLogger(ContinuationGames.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public static void main(String[] args) throws Exception {   
        BProgram bpp = new BPP();
        new BProgramRunner(bpp).start();
    }
}
