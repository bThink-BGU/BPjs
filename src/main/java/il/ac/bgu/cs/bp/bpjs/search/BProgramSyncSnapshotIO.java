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
package il.ac.bgu.cs.bp.bpjs.search;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.TopLevel;
import org.mozilla.javascript.serialize.ScriptableInputStream;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

/**
 * Serialized and de-serializes {@link BProgramSyncSnapshot}s.
 *
 * @author michael
 */
public class BProgramSyncSnapshotIO {

    private static class Header implements java.io.Serializable {

        public Header(int bthreadCount, int externalEventCount) {
            this.bthreadCount = bthreadCount;
            this.externalEventCount = externalEventCount;
        }
        
        final int bthreadCount;
        final int externalEventCount;
    }

    private BProgram bprogram;

    public BProgramSyncSnapshotIO(BProgram bprogram) {
        this.bprogram = bprogram;
    }

    public byte[] serialize(BProgramSyncSnapshot bpss) throws IOException {
        try {
            Context ctxt = Context.enter(); // need Javascript environment for this, even though we're not executing code per se.
            
            // first, get bp out of the scope
            bpss.getBThreadSnapshots().forEach(bss->removeBpReference(bss));            

            // second, serialize
            final Scriptable topLevelScope = ctxt.initSafeStandardObjects();
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ScriptableOutputStream outs = new ScriptableOutputStream(bytes, topLevelScope)) {
                outs.writeObject( new Header(bpss.getBThreadSnapshots().size(), bpss.getExternalEvents().size()) );
                for ( BThreadSyncSnapshot bss : bpss.getBThreadSnapshots() ) {
                    outs.writeObject(bss);
                }
                for ( BEvent ee:bpss.getExternalEvents() ) {
                    outs.writeObject(ee);
                }
                outs.flush();
                
                // restore scope
                bpss.getBThreadSnapshots().forEach(bss->addBpReference(bss));            
                
                return bytes.toByteArray();
            }
        } finally {
            Context.exit();
        }
    }
    
    public BProgramSyncSnapshot deserialize( byte[] bytes ) throws IOException, ClassNotFoundException {
        try {
            Context ctxt = ContextFactory.getGlobal().enterContext();
            ctxt.setOptimizationLevel(-1); // must use interpreter mode
            final Scriptable topLevelScope = ctxt.initSafeStandardObjects(new TopLevel());
            
            try ( ScriptableInputStream sis = new ScriptableInputStream(new ByteArrayInputStream(bytes), topLevelScope)) {
                Header header = (Header) sis.readObject();
                
                Set<BThreadSyncSnapshot> bthreads = new HashSet<>(header.bthreadCount);
                for ( int i=0; i<header.bthreadCount; i++ ) {
                    bthreads.add((BThreadSyncSnapshot) sis.readObject());
                }
                
                List<BEvent> events = new ArrayList<>(header.externalEventCount);
                for ( int i=0; i<header.externalEventCount; i++ ) {
                    events.add((BEvent) sis.readObject());
                }
                
                bthreads.forEach( bss -> addBpReference(bss) );
                return new BProgramSyncSnapshot(bprogram, bthreads, events);
            }
        } finally {
            Context.exit();
        }
    }
    
    private void addBpReference(BThreadSyncSnapshot bss) {
        Scriptable scope = bss.getScope();
        while ( scope.getParentScope() != null ) {
            scope = scope.getParentScope();
        }
        scope.put("bp", scope, new BProgramJsProxy(bprogram));
    }

    private void removeBpReference(BThreadSyncSnapshot bss) {
        for (Scriptable sc = bss.getScope(); sc != null; sc = sc.getParentScope()) {
            if (sc.has("bp", sc)) {
                sc.delete("bp");
            }
        }
    }
    
}
