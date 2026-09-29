/*
 * The MIT License
 *
 * Copyright 2025 michael.
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
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.EventSetsJsProxy;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeSet;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptRuntime;

/**
 * Stub provider for standard classes.
 * @author michael
 */
public class BuiltInStubberFactory implements SerializationStubberFactory {

    @Override
    public SerializationStubber build(BProgram aBProgram) {
        return new BuiltInStubber(aBProgram);
    }
        
}

class BuiltInStubber implements SerializationStubber {

    public static final StreamObjectStub BP_PROXY = new StreamObjectStub("built-in", "BP-PROXY");
    public static final StreamObjectStub ES_PROXY = new StreamObjectStub("built-in", "ES-PROXY");
    
    
    /**
     * Rhino's compiled regular expression class. It is package-private, so we
     * can't reference it directly.
     */
    static final Class<?> RECOMPILED_CLASS;
    
    static {
        try {
            RECOMPILED_CLASS = Class.forName("org.mozilla.javascript.regexp.RECompiled");
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    private final BProgramJsProxy bprogProxy;
    
    BuiltInStubber( BProgram aBProg ) {
        bprogProxy = new BProgramJsProxy(aBProg);
    }
    
    @Override
    public String getId() {
        return "built-in";
    }

    @Override
    public Set<Class> getClasses() {
        return Set.of( BProgramJsProxy.class, EventSetsJsProxy.class, Optional.class, NativeSet.class, RECOMPILED_CLASS );
    }

    @Override
    public StreamObjectStub stubFor(Object in) {
        if ( in instanceof BProgramJsProxy  ) return BP_PROXY;
        if ( in instanceof EventSetsJsProxy ) return ES_PROXY;
        if ( in instanceof Optional ) {
            Optional opt = (Optional) in;
            return (StreamObjectStub)opt.map( v -> new OptionalStub(v) ).orElse(OptionalStub.EMPTY);
        }
        if ( in instanceof NativeSet ) {
            return NativeSetStub.forSet((NativeSet) in);
        }
        if ( RECOMPILED_CLASS.isInstance(in) ) {
            return RegExpStub.forCompiled(in);
        }
        throw new IllegalArgumentException("BuiltInStubber cannot handle " + Objects.toString(in));
    }

    @Override
    public Object objectFor(StreamObjectStub aStub) {
        if ( aStub.equals(BP_PROXY) ) return bprogProxy;
        if ( aStub.equals(ES_PROXY) ) return EventSetsJsProxy.INSTANCE;
        if ( aStub.equals(OptionalStub.EMPTY) ) return Optional.empty();
        if ( aStub instanceof OptionalStub ) {
            return Optional.ofNullable(aStub.getData());
        }
        if ( aStub instanceof NativeSetStub ) {
            return ((NativeSetStub)aStub).deStub();
        }
        if ( aStub instanceof RegExpStub ) {
            return ((RegExpStub)aStub).deStub();
        }
        throw new IllegalArgumentException("BuiltInStubber cannot handle " + aStub.toString());
    }
    
    
    
    static class OptionalStub extends StreamObjectStub implements java.io.Serializable {

        public static final OptionalStub EMPTY = new OptionalStub(null);

        public OptionalStub(Object value) {
            super("built-in", value);
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
            return super.equals(obj);
        }
    }
    
    static class NativeSetStub extends StreamObjectStub implements java.io.Serializable {
        
        
        public static NativeSetStub forSet(NativeSet toStub) {

            String code = "ns.forEach(e=>javaSet.add(e))";
            Set<?> javaSet = new HashSet<>();
            try ( Context cx = BPjs.enterRhinoContext()) {
                Scriptable tlScope = BPjs.makeBPjsSubScope();
                tlScope.put("ns", tlScope, toStub);
                tlScope.put("javaSet", tlScope, javaSet);
                cx.evaluateString(tlScope, code, "", 1, null);
            }
            return new NativeSetStub(javaSet);
        }
        
        public NativeSetStub(Set<?> javaSet) {
            super("built-in", javaSet);
        }

        public Object deStub(){

            String src = "let ns = new Set(); javaSet.forEach(e=>ns.add(e));";

            try (Context cx = BPjs.enterRhinoContext()) {
                Scriptable tlScope = BPjs.makeBPjsSubScope();
                tlScope.put("javaSet", tlScope, getData());
                cx.evaluateString( tlScope, src, "", 1, null);
                return tlScope.get("ns", tlScope);
            }

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
            return super.equals(other);
        }
    }
    
    /**
     * Stubs compiled regular expressions, which Rhino 1.9.1 cannot serialize
     * (their character classes hold a non-serializable {@code NativeRegExp.ClassContents}).
     * 
     * We stub {@code RECompiled} rather than {@code NativeRegExp}: regex literals are
     * compiled into {@code RECompiled} objects stored in the function's interpreter data,
     * so they get serialized even when no {@code NativeRegExp} exists. Live
     * {@code NativeRegExp}s serialize normally once their {@code RECompiled} is stubbed.
     * 
     * The stub holds the regex in its literal form ({@code /source/flags}).
     */
    static class RegExpStub extends StreamObjectStub implements java.io.Serializable {
        
        public static RegExpStub forCompiled(Object reCompiled) {
            try (Context cx = BPjs.enterRhinoContext()) {
                // NativeRegExp.toString() yields the literal form, "/source/flags".
                String literal = ScriptRuntime.checkRegExpProxy(cx)
                    .wrapRegExp(cx, BPjs.getBPjsScope(), reCompiled).toString();
                return new RegExpStub(literal);
            }
        }
        
        public RegExpStub(String literal) {
            super("built-in", literal);
        }
        
        public Object deStub() {
            String literal = (String) getData();
            int flagsStart = literal.lastIndexOf('/');
            String source = literal.substring(1, flagsStart);
            String flags = literal.substring(flagsStart + 1);
            try (Context cx = BPjs.enterRhinoContext()) {
                return ScriptRuntime.checkRegExpProxy(cx).compileRegExp(cx, source, flags);
            }
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
            return super.equals(obj);
        }
    }
    
}
