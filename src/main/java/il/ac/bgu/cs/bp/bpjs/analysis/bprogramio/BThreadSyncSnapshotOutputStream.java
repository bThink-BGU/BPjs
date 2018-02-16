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
package il.ac.bgu.cs.bp.bpjs.analysis.bprogramio;

import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BThreadJsProxy;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

/**
 * Output stream for {@link BThreadSyncSnapshot} objects. Creates stubs for objects that can link back to the java
 * context, such as the {@code XJsProxy} classes.
 * 
 * @author michael
 */
public class BThreadSyncSnapshotOutputStream extends ScriptableOutputStream {
    
    private final List<StreamObjectStub> stubs = new ArrayList<>();
    
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public BThreadSyncSnapshotOutputStream(OutputStream out, Scriptable scope) throws IOException {
        super(out, scope);
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
        if ( obj instanceof BThreadJsProxy ) {
            stubs.add(StreamObjectStub.BT_PROXY);
            return StreamObjectStub.BT_PROXY;
            
        } else if ( obj instanceof BProgramJsProxy ) {
            stubs.add(StreamObjectStub.BP_PROXY);
            return StreamObjectStub.BP_PROXY;
            
        } else {
            return obj;
        }
    }

    public List<StreamObjectStub> getStubs() {
        return stubs;
    }
    
}
