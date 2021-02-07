/*
 * The MIT License
 *
 * Copyright 2021 michael.
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
package il.ac.bgu.cs.bp.bpjs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Central place for application-level, static BPjs resources.
 * 
 * @author michael
 */
public class BPjs {
    
    /**
     * Top-level scope for all BPjs code.
     */
    private static ScriptableObject BPJS_SCOPE;
    
    static {
        makeBPjsScope();
    }
    
    /**
     * Initializes a Rhino context fit for BPjs execution. This means, for
     * example, setting the optimization level to -1, and the ES version.
     * 
     * <B>NOTE</B> Must call {@code Context.exit()} after calling this method. 
     * Preferably, call this method in a {@code try} block, and have the related
     * {@code Context.exit()} call in a {@code finally} block.
     * 
     * @return A Rhino context for BPjs executions.
     */
    public static Context enterRhinoContext() {
        Context cx = Context.enter();
        cx.setOptimizationLevel(-1); // must use interpreter mode for continuations to work
        if ( cx.getLanguageVersion() != Context.VERSION_ES6 ) {
            cx.setLanguageVersion(Context.VERSION_ES6);
        }
        return cx;
    }
    
    /**
     * Returns a sealed, frozen scope that all BPjs code and evaluations run 
     * under.
     * 
     * @return The top-level scope for all BPjs code.
     */
    public static ScriptableObject getBPjsScope() {
        return BPJS_SCOPE;
    }
    
    /**
     * Creates a new scope for running BPjs code. This scope's parent scope
     * is the JVM-global BPjs scope.
     * 
     * @return A new scope to run BPjs code in.
     * @see #getBPjsScope() 
     */
    public static Scriptable makeBPjsSubScope() {
        Context cx = enterRhinoContext();
        Scriptable retVal = cx.newObject(BPjs.getBPjsScope());
        retVal.setPrototype(BPjs.getBPjsScope());
        retVal.setParentScope(null);
        return retVal;
    }
    
    private static void makeBPjsScope() {
        try {
            Context cx = enterRhinoContext();
            ImporterTopLevel importer = new ImporterTopLevel(cx);
            BPJS_SCOPE = cx.initStandardObjects(importer, true); // create and seal
            // NOTE: global extensions to BPjs scopes would go here, if we decide to create them.
        } finally {
            Context.exit();
        }
    }
}
