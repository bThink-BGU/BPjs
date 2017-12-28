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

/**
 * Placeholders to put in the application stream. Used to break connections from the
 * javascript serialized context to the Java context.
 * 
 * 
 * @author michael
 */
public class StreamObjectStub implements java.io.Serializable {
    
    public static final StreamObjectStub BP_PROXY = new StreamObjectStub("bp-proxy");
    public static final StreamObjectStub BT_PROXY = new StreamObjectStub("bt-proxy");
    
    private final String name;

    public StreamObjectStub(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "[Stub: " + name + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj == null ) return false;
        if ( obj == this ) return true;
        if ( ! (obj instanceof StreamObjectStub) ) return false;
        return ((StreamObjectStub)obj).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    private Object readResolve() {
        if ( name.equals(BP_PROXY.name) ) return BP_PROXY;
        if ( name.equals(BT_PROXY.name) ) return BT_PROXY;
        return this;
    }
    
}
