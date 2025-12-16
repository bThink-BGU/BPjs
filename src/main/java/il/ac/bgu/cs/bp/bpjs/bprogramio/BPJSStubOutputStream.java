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
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import org.mozilla.javascript.serialize.ScriptableOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Output stream for {@link BThreadSyncSnapshot} objects. Creates stubs for
 * objects that can link back to the java context, such as the {@code XJsProxy}
 * classes.
 *
 * @author michael
 */
public class BPJSStubOutputStream extends ScriptableOutputStream {

    private final Map<Class, SerializationStubber> stubbers = new HashMap<>();
    
    /**
     * Constructor
     * @param out downstream
     * @param someStubbers stubbers to apply
     * @throws IOException 
     */
    public BPJSStubOutputStream(OutputStream out, Set<SerializationStubber> someStubbers) throws IOException {
        super(out, BPjs.getBPjsScope());
        for ( var stb : someStubbers ) {
            for ( var clz : stb.getClasses() ) {
                if ( stubbers.containsKey(clz) ) {
                    BPjs.log().warn("Stubber " + stb.getId() + " overrides stubber for class " + clz.getCanonicalName() + ", previously set by " + stubbers.get(clz).getId() );
                }
                stubbers.put(clz, stb);
            }
        }
    }

    @Override
    protected Object replaceObject(Object obj) throws IOException {
        if (obj == null) {
            return null;
        }
        SerializationStubber srs = stubbers.get(obj.getClass());
        return srs == null ? super.replaceObject(obj) : srs.stubFor(obj);
    }
    
}
