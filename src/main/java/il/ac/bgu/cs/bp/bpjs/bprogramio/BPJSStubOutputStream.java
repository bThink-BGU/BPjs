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
package il.ac.bgu.cs.bp.bpjs.bprogramio;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeSet;

/**
 * Output stream for {@link BThreadSyncSnapshot} objects. Creates stubs for
 * objects that can link back to the java context, such as the {@code XJsProxy}
 * classes.
 *
 * @author michael
 */
public class BPJSStubOutputStream extends ScriptableOutputStream {

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BPJSStubOutputStream(OutputStream out) throws IOException {
        super(out, BPjs.getBPjsScope());

    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BProgramJsProxy) {
            return StreamObjectStub.BP_PROXY;

        } else if (obj instanceof Optional) {
            return OptionalStub.getFor((Optional) obj);

        } else if (obj instanceof NativeSet) {
            NativeSet ns = (NativeSet) obj;
            return stubify(ns);

        } else {
            return super.replaceObject(obj);
        }
    }

    /**
     * NativeSet does not have good Java accessors, so we use a JavaScript code
     * to extract its members.
     *
     * @param ns a NativeSet
     * @return a NativeSetStub with the values of ns.
     */
    private NativeSetStub stubify(NativeSet ns) {

        String code = "ns.forEach(e=>javaSet.add(e))";

        try ( Context cx = BPjs.enterRhinoContext()) {
            Scriptable tlScope = BPjs.makeBPjsSubScope();
//            ScriptableObject tlScope = cx.initStandardObjects();
            Set<Object> javaSet = new HashSet<>();
            tlScope.put("ns", tlScope, ns);
            tlScope.put("javaSet", tlScope, javaSet);
            cx.evaluateString(tlScope, code, "", 1, null);

            return new NativeSetStub(javaSet);
        }
    }
}
