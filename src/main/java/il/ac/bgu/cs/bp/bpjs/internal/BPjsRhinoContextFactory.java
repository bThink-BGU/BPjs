/*
 * The MIT License
 *
 * Copyright 2022 michael.
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
package il.ac.bgu.cs.bp.bpjs.internal;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

/**
 * A context factory for all BPjs' contexts.
 *
 * @author michael
 */
public class BPjsRhinoContextFactory extends ContextFactory {
    
    @Override
    protected boolean hasFeature(Context cx, int featureIndex) {
        switch (featureIndex) {
            case Context.FEATURE_LOCATION_INFORMATION_IN_ERROR:
                return true;

            case Context.FEATURE_INTEGER_WITHOUT_DECIMAL_PLACE:
                return true;

            case Context.FEATURE_ENABLE_JAVA_MAP_ACCESS:
                return true;
            
            case Context.FEATURE_THREAD_SAFE_OBJECTS:
                return true;
            
            default:
                return super.hasFeature(cx, featureIndex);
        }
    }

    @Override
    protected Context makeContext() {
        Context cx = super.makeContext();
        cx.setInterpretedMode(true); // must use interpreter mode for continuations to work
        if (cx.getLanguageVersion() != Context.VERSION_ECMASCRIPT) {
            cx.setLanguageVersion(Context.VERSION_ECMASCRIPT);
        }
        cx.setWrapFactory(BPjsWrapFactory.INSTANCE);
        return cx;
    }
}

/**
 * Returns java objects for primitive types, to allow easier state-based comparison.
 * 
 * @author michael
 */
class BPjsWrapFactory extends WrapFactory {
    
    static final BPjsWrapFactory INSTANCE = new BPjsWrapFactory();
    
    @Override
    public Object wrap(Context cx, Scriptable scope, Object obj, Class<?> staticType) {
        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
            return obj;

        } else if (obj instanceof Character) {
            char[] a = {((Character) obj)};
            return new String(a);

        }
        return super.wrap(cx, scope, obj, staticType);
    }
}
