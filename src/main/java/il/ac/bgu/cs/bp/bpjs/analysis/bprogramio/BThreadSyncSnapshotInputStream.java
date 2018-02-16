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

import java.io.IOException;
import java.io.InputStream;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.serialize.ScriptableInputStream;

/**
 *
 * @author michael
 */
class BThreadSyncSnapshotInputStream extends ScriptableInputStream {
    
    private final StubProvider stubProvider;
    
    public BThreadSyncSnapshotInputStream(InputStream in, Scriptable scope, StubProvider aProvider) throws IOException {
        super(in, scope);
        stubProvider = aProvider;
    }

    @Override
    protected Object resolveObject(Object obj) throws IOException {
        return ( obj instanceof StreamObjectStub )
            ? stubProvider.get((StreamObjectStub) obj)
            : obj;
    }

    @Override
    protected Object readObjectOverride() throws IOException, ClassNotFoundException {
        return super.readObjectOverride();
    }
    
}
