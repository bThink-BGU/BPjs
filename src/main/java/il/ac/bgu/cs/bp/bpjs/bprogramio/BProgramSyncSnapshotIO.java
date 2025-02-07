/*
 * The MIT License
 *
 * Copyright 2017 BGU.
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
package il.ac.bgu.cs.bp.bpjs.bprogramio;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.SafetyViolationTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

/**
 * Serialized and de-serializes {@link BProgramSyncSnapshot}s.
 *
 * @author michael
 */
public class BProgramSyncSnapshotIO {

    private static class Header implements java.io.Serializable {

        public Header(int bthreadCount, int externalEventCount, SafetyViolationTag fa) {
            this.bthreadCount = bthreadCount;
            this.externalEventCount = externalEventCount;
            this.fa = fa;
        }

        final int bthreadCount;
        final int externalEventCount;
        final SafetyViolationTag fa;
    }

    private final BProgram bprogram;
    private final Set<SerializationStubber> stubbers = new HashSet<>();

    public BProgramSyncSnapshotIO(BProgram bprogram) {
        this.bprogram = bprogram;
        for ( var f : BPjs.getRegisteredStubberFactories() ) {
            stubbers.add( f.build(bprogram) );
        }
    }

    public byte[] serialize(BProgramSyncSnapshot bpss) throws IOException {
        try (Context cx = BPjs.enterRhinoContext()) {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                BPJSStubOutputStream outs = newOutputStream(bytes)
            ) {

                outs.writeObject(new Header(bpss.getBThreadSnapshots().size(), bpss.getExternalEvents().size(), bpss.getViolationTag()));

                outs.writeObject(bpss.getDataStore());
                
                for (BThreadSyncSnapshot bss : bpss.getBThreadSnapshots()) {
                    writeBThreadSnapshot(bss, outs);
                }

                for (BEvent ee : bpss.getExternalEvents()) {
                    outs.writeObject(ee);
                }
                outs.flush();

                return bytes.toByteArray();
            }
        }
    }

    public BProgramSyncSnapshot deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (Context cx = BPjs.enterRhinoContext()) {
            try (BPJSStubInputStream in = newInputStream(new ByteArrayInputStream(bytes))
            ) {
                Header header = (Header) in.readObject();
                
                Map<String, Object> dataStore = (Map<String, Object>) in.readObject();
                
                Set<BThreadSyncSnapshot> bthreads = new HashSet<>(header.bthreadCount);

                for (int i = 0; i < header.bthreadCount; i++) {
                    bthreads.add(readBThreadSnapshot(in, dataStore));
                }

                List<BEvent> externalEvents = new ArrayList<>(header.externalEventCount);
                for (int i = 0; i < header.externalEventCount; i++) {
                    externalEvents.add((BEvent) in.readObject());
                }

                return new BProgramSyncSnapshot(bprogram, bthreads, dataStore, externalEvents, header.fa);
            }
        }
    }
    
    public byte[] serializeBThread(BThreadSyncSnapshot btss) {
        try (Context cx = BPjs.enterRhinoContext()) {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                BPJSStubOutputStream outs = newOutputStream(bytes) 
            ) {
                writeBThreadSnapshot(btss, outs);
                outs.flush();
                return bytes.toByteArray();
                
            } catch (IOException ex) {
                // all read/writes are in-memory.
                throw new RuntimeException("IO exception serializing a b-thread:"+ex.getMessage(), ex);
            }
        }   
    }
    
    public BThreadSyncSnapshot deserializeBThread( byte[] serializedBT, Map<String,Object> bprogramDataStore  ) {
        try (Context cx = BPjs.enterRhinoContext()) {
            try (BPJSStubInputStream sis = newInputStream(new ByteArrayInputStream(serializedBT))) {
                return readBThreadSnapshot(sis, bprogramDataStore);
            } catch (ClassNotFoundException|IOException ex) {
                throw new RuntimeException("Error reading a serialized b-thread: " + ex.getMessage(), ex );
            }
        }
    }
    
    /**
     * Create a properly stubbed output stream.
     * @param downstream The stream to write into.
     * @return A stubbed stream, based on BPjs's registered stub factories.
     * @throws IOException 
     */
    public BPJSStubOutputStream newOutputStream(OutputStream downstream) throws IOException {
        return new BPJSStubOutputStream(downstream, stubbers);
    }
    
    /**
     * Create a properly stubbed input stream
     * @param upstream the stream to read from
     * @return an input stream that de-stubs stream stubs.
     * @throws IOException 
     */
    public BPJSStubInputStream newInputStream(InputStream upstream) throws IOException {
        return new BPJSStubInputStream(upstream, stubbers);
    }
    
    private void writeBThreadSnapshot(BThreadSyncSnapshot bss, BPJSStubOutputStream outs) throws IOException {
        try {
            outs.writeObject(bss.getName());
            outs.writeObject(bss.getEntryPoint());
            outs.writeObject(bss.getInterrupt().orElse(null));
            outs.writeObject(bss.getSyncStatement());
            outs.writeObject(bss.getData());
            outs.writeObject(bss.getStorageModifications());
            outs.writeObject(bss.getContinuation());
        } catch ( IOException iox ) {
            throw new IOException("While serializing b-thread '" + bss.getName() + "': " + iox.getMessage(), iox.getCause());
        }
    }

    private BThreadSyncSnapshot readBThreadSnapshot(BPJSStubInputStream sis, Map<String,Object> bprogramDataStore) throws IOException, ClassNotFoundException {
        String name = (String) sis.readObject();
        Function entryPoint = (Function) sis.readObject();
        Function interruptHandler = (Function) sis.readObject();
        SyncStatement stmt = (SyncStatement) sis.readObject();
        Object data = sis.readObject();
        Object modifications = sis.readObject();
        Object cont = sis.readObject();

        MapProxy proxy = new MapProxy(bprogramDataStore, (Map<String, MapProxy.Modification<Object>>) modifications);
        final BThreadSyncSnapshot bThreadSyncSnapshot = new BThreadSyncSnapshot(name, entryPoint, interruptHandler, cont, stmt, data, proxy);

        return bThreadSyncSnapshot;
    }
    
}
