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
package il.ac.bgu.cs.bp.bpjs.analysis.bprogramio;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BThreadJsProxy;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.serialize.ScriptableInputStream;

/**
 * Serialized and de-serializes {@link BProgramSyncSnapshot}s.
 *
 * @author michael
 */
public class BProgramSyncSnapshotIO {

    private static class Header implements java.io.Serializable {

        public Header(int bthreadCount, int externalEventCount, FailedAssertion fa) {
            this.bthreadCount = bthreadCount;
            this.externalEventCount = externalEventCount;
            this.fa = fa;
        }

        final int bthreadCount;
        final int externalEventCount;
        final FailedAssertion fa;
    }

    private final BProgram bprogram;

    public BProgramSyncSnapshotIO(BProgram bprogram) {
        this.bprogram = bprogram;
    }

    public byte[] serialize(BProgramSyncSnapshot bpss) throws IOException {
        try {
            Context ctxt = Context.enter(); // need Javascript environment for
            // this, even though we're not
            // executing code per se.

            final ScriptableObject globalScope = ctxt.initStandardObjects();

            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream outs = new ObjectOutputStream(bytes)) {

                outs.writeObject(new Header(bpss.getBThreadSnapshots().size(), bpss.getExternalEvents().size(), bpss.getFailedAssertion()));

                for (BThreadSyncSnapshot bss : bpss.getBThreadSnapshots()) {
                    writeBThreadSnapshot(bss, outs, globalScope);
                }

                for (BEvent ee : bpss.getExternalEvents()) {
                    outs.writeObject(ee);
                }
                outs.flush();

                return bytes.toByteArray();
            }

        } finally {
            Context.exit();
        }
    }

    public BProgramSyncSnapshot deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try {
            Context ctxt = ContextFactory.getGlobal().enterContext();

            // must use interpreter mode
            ctxt.setOptimizationLevel(-1);
            final ScriptableObject globalScope = ctxt.initStandardObjects();

            try (ScriptableInputStream sis
                = new ScriptableInputStream(new ByteArrayInputStream(bytes), globalScope)) {
                Header header = (Header) sis.readObject();

                Set<BThreadSyncSnapshot> bthreads = new HashSet<>(header.bthreadCount);

                for (int i = 0; i < header.bthreadCount; i++) {
                    bthreads.add(readBThreadSnapshot(sis, globalScope));
                }

                List<BEvent> events = new ArrayList<>(header.externalEventCount);
                for (int i = 0; i < header.externalEventCount; i++) {
                    events.add((BEvent) sis.readObject());
                }

                return new BProgramSyncSnapshot(bprogram, bthreads, events, header.fa);
            }
        } finally {
            Context.exit();
        }
    }

    private void writeBThreadSnapshot(BThreadSyncSnapshot bss, ObjectOutputStream outs, ScriptableObject scope) throws IOException {
        outs.writeObject(bss.getName());

        ByteArrayOutputStream bthreadBytes = new ByteArrayOutputStream();
        try (BThreadSyncSnapshotOutputStream btos = new BThreadSyncSnapshotOutputStream(bthreadBytes, scope)) {
            btos.writeObject(bss.getScope());
            btos.writeObject(bss.getEntryPoint());
            btos.writeObject(bss.getInterrupt().orElseGet(() -> null));
            btos.writeObject(bss.getBSyncStatement());

            btos.writeObject(bss.getContinuation());
            btos.flush();
        }
        bthreadBytes.flush();
        bthreadBytes.close();
        outs.writeObject(bthreadBytes.toByteArray());
    }

    private BThreadSyncSnapshot readBThreadSnapshot(ScriptableInputStream sis, ScriptableObject scope) throws IOException, ClassNotFoundException {
        String name = (String) sis.readObject();
        byte[] contBytes = (byte[]) sis.readObject();

        final BThreadJsProxy btProxy = new BThreadJsProxy();
        final BProgramJsProxy bpProxy = new BProgramJsProxy(bprogram);

        StubProvider stubPrv = (StreamObjectStub stub) -> {
            if (stub == StreamObjectStub.BP_PROXY) {
                return bpProxy;
            }
            if (stub == StreamObjectStub.BT_PROXY) {
                return btProxy;
            }
            throw new IllegalArgumentException("Unknown stub " + stub);
        };

        try (ByteArrayInputStream inBytes = new ByteArrayInputStream(contBytes);
            BThreadSyncSnapshotInputStream bssis = new BThreadSyncSnapshotInputStream(inBytes, scope, stubPrv)) {
            Scriptable btScope = (Scriptable) bssis.readObject();
            Function entryPoint = (Function) bssis.readObject();
            Function interruptHandler = (Function) bssis.readObject();
            BSyncStatement stmt = (BSyncStatement) bssis.readObject();
            Object cont = bssis.readObject();
            final BThreadSyncSnapshot bThreadSyncSnapshot = new BThreadSyncSnapshot(name, entryPoint, interruptHandler, btScope, cont, stmt);

            btProxy.setBThread(bThreadSyncSnapshot);
            return bThreadSyncSnapshot;
        }

    }

}
