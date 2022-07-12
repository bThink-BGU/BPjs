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
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Placeholders to put in the application stream. Used to break connections from the
 * JavaScript serialized context to the Java context.
 * 
 * 
 * @author michael
 */
public class StreamObjectStub implements java.io.Serializable {
    
    public static final StreamObjectStub BP_PROXY = new StreamObjectStub("bp-proxy");
    
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
        return this;
    }    
}

class OptionalStub extends StreamObjectStub implements java.io.Serializable {

    public static final OptionalStub EMPTY = new OptionalStub(null);
    
    static OptionalStub getFor(Optional<?> optional) {
        return optional.map(e->new OptionalStub(e)).orElse(EMPTY);
    }
    
    public final Object value;

    public OptionalStub(Object value) {
        super("OptionalStub");
        this.value = value;
    }
    
    private Object readResolve() {
        return Optional.ofNullable(value);
    }

    @Override
    public int hashCode() {
        return 5*Objects.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OptionalStub other = (OptionalStub) obj;
        return Objects.equals(this.value, other.value);
    }
}

class NativeSetStub extends StreamObjectStub implements java.io.Serializable {
    
    public final Set<Object> items;

    public NativeSetStub(Set<Object> someItems) {
        super("NativeSetStub");
        items = someItems;
    }
    
    private Object readResolve(){
        
        String src = "let ns = new Set(); javaSet.forEach(e=>ns.add(e)); ns";
        
        try (Context cx = BPjs.enterRhinoContext()) {
            Scriptable tlScope = BPjs.makeBPjsSubScope();
            Set<Object> javaSet = new HashSet<>();
            tlScope.put("javaSet", tlScope, javaSet);
            return cx.evaluateString( tlScope, src, "", 1, null);
        }
        
    }
    
    @Override
    public int hashCode() {
        return 7*Objects.hashCode(this.items);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NativeSetStub other = (NativeSetStub) obj;
        return Objects.equals(this.items, other.items);
    }
}