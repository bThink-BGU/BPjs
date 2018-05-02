/*
 * The MIT License
 *
 * Copyright 2017 michael.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package il.ac.bgu.cs.bp.bpjs.mains;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import java.util.HashMap;
import java.util.Map;
import org.mozilla.javascript.NativeContinuation;
import org.mozilla.javascript.ScriptRuntime;

/**
 * Playing around with continuations.
 *
 * @author michael
 */
public class ContinuationGames {
    
    static final String SRC = 
                      "gVar=1;\n" 
                    + "bp.registerBThread( \"bt\", function(){\n"
                    + "   bp.log.info(\"started\");"
                    + "   var bVar=1;"
                    + "   var evt = bsync({request: bp.Event(\"e\")});\n"
                    + "   bp.log.info('gVar:' + gVar + ' bVar:'+bVar + ' event.name:' + evt.name);"
                    + "   gVar = gVar+1;"
                    + "   bVar = bVar+1;"
                    + "});";
    
    public static void main(String[] args) throws Exception {   
        
        // Create a program
        BProgram bprog = new StringBProgram(SRC);
        
        // Run the top-level code (b-threads are registered but not yet run)
        BProgramSyncSnapshot cur = bprog.setup();
        
        // Run to first bsync
        cur = cur.start( ExecutorServiceMaker.makeWithName("TEST"));
        
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
                if ( sc.has("j", sc) ) {
                    System.out.println("Found j:" + sc.get("j", sc));
                }
                System.out.println("SCOPE END\n");
            }
            
            // second, serialize
            final Scriptable topLevelScope = ctxt.initSafeStandardObjects();
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); 
                 ScriptableOutputStream outs = new ScriptableOutputStream(bytes, topLevelScope)) {
                outs.writeObject(cnt);
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

                    // re-add bp to the scope
                    cnt2.getParentScope().put("bp", cnt2.getParentScope(), new BProgramJsProxy(bprog));

                    // go - we can push whichever event we want.
                    ctxt.resumeContinuation(cnt2, cnt2, new BEvent("e-"+i));
                    
                    // this extra run will use the same control flow, but the variable
                    // values will be from the previous run. So don't do this :-)
                    ctxt.resumeContinuation(cnt2, cnt2, new BEvent("arbitrary/"+i));
                }
            }   
            
            // create three continuation objects that should be the same.
            Scriptable[] cnts = new Scriptable[3];
            for (int i = 0; i < 3; i++) {
                try ( ScriptableInputStream sis = new ScriptableInputStream(new ByteArrayInputStream(serializedContinuationAndScope), topLevelScope)) {
                    // read cnt and scope
                    cnts[i] = (Scriptable) sis.readObject();
                }
            }
            
            System.out.println(cnts[0].equals(cnts[0]));
            System.out.println(cnts[0].equals(cnts[1]));
            
            System.out.println("continuationsEq = " + continuationEq( (NativeContinuation)cnts[0], (NativeContinuation)cnts[1]) );
            
        } finally {
            Context.exit();
        }
    }
    
    static boolean continuationEq( NativeContinuation a, NativeContinuation b ) {
        // same heap
        if ( ! ScriptRuntime.shallowEq(a.getParentScope(), b.getParentScope()) ) {
            Map<Object,Object> scopeA = scopeValues(a);
            Map<Object,Object> scopeB = scopeValues(b);
            System.out.println("scopeA = " + scopeA);
            System.out.println("scopeB = " + scopeB);
            System.out.println("scopeB.equals(scopeA) = " + scopeB.equals(scopeA));
            return false;
        }
        System.out.println("Same parent scope.");
        
        // same stack
        System.out.println("implementation = " + a.getImplementation() );
        
        // same pc
        
        return true;
    }
    
    static Map<Object, Object> scopeValues(NativeContinuation nc ){
        Map<Object, Object> retVal = new HashMap<>();
        
        Scriptable current = nc;
        // traverse prototype and parent chains, according to lookup algorithm.
        for ( Object o : nc.getIds() ) {
            retVal.put(o, nc.get(o));
        }
        return retVal;
    }
}
