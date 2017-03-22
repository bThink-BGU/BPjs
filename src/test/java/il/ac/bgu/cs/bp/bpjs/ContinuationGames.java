/*
 *  Author: Michael Bar-Sinai
 */
package il.ac.bgu.cs.bp.bpjs;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy.BProgramJsProxy;
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
    
    static final String SRC = "j=1;\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   bp.log.info(\"started\");"
                    + "   var i=1;"
                    + "   bsync({request: bp.Event(\"e\")});\n"
                    + "   bp.log.info('i:' + i + ' j:'+j);"
                    + "   i = i+1;"
                    + "   j = j+1;"
                    + "});";
    
    public static void main(String[] args) throws Exception {   
        
        // Create a program
        BProgram bprog = new StringBProgram(SRC);
        
        // Run the top-level code (b-threads are registered but not yet run)
        BProgramSyncSnapshot cur = bprog.setup();
        
        // Run to first bsync
        cur = cur.start();
        
        // Get a snapshot
        final BThreadSyncSnapshot snapshot = cur.getBThreadSnapshots().iterator().next();
        System.out.println(snapshot);
        
        // Serialize snapshot
        byte[] serializedContinuationAndScope = null;
        Object bp = null;
        try {
            Context ctxt = Context.enter(); // need Javascript environment for this, even though we're not executing code per se.
            
            // first, get bp out of the scope
            Object cnt = snapshot.getContinuation();
            final Scriptable scope = snapshot.getScope();

            for ( Scriptable sc=scope; sc!=null; sc = sc.getParentScope() ) {
                System.out.println("SCOPE START");
                if ( sc.has("bp", sc) ) {
                    bp = sc.get("bp", sc);
                    sc.delete("bp");
                    System.out.println("bp deleted.");
                }
                System.out.println("SCOPE END\n\n");
            }
            
            // second, serialize
            final Scriptable topLevelScope = ctxt.initSafeStandardObjects();
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); 
                 ScriptableOutputStream outs = new ScriptableOutputStream(bytes, topLevelScope)) {
                outs.writeObject(cnt);
                outs.writeObject(scope);
                outs.flush();
                serializedContinuationAndScope = bytes.toByteArray();
            }
            System.out.println("Seriazlied to " + serializedContinuationAndScope.length + " bytes.");
        } finally {
            Context.exit();
        }
        
        // Run the BThread a few times:
        try {
            Context ctxt = ContextFactory.getGlobal().enterContext();
            ctxt.setOptimizationLevel(-1); // must use interpreter mode
            final Scriptable topLevelScope = ctxt.initSafeStandardObjects();
            
            for (int i = 0; i < 10; i++) {
                try ( ScriptableInputStream sis = new ScriptableInputStream(new ByteArrayInputStream(serializedContinuationAndScope), topLevelScope)) {
                    // read cnt and scope
                    Scriptable cnt2 = (Scriptable) sis.readObject();
                    Scriptable scope2 = (Scriptable) sis.readObject();

                    // re-add bp to the scope
                    bprog.getGlobalScope().put("bp", bprog.getGlobalScope(), bp);
                    scope2.setParentScope(bprog.getGlobalScope());

                    // go!!
                    ctxt.resumeContinuation(cnt2, scope2, new Object[0]);
                    ctxt.resumeContinuation(cnt2, scope2, new Object[0]);
                }
            }   
        } finally {
            Context.exit();
        }
    }
    
}
