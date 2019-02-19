/*
 * The MIT License
 *
 * Copyright 2018 michael.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Some methods to make working with {@link Scriptable} easier.
 * @author michael
 */
public class ScriptableUtils {
    
    public static Scriptable getPenultiamteParent( Scriptable aScope ) {
        Scriptable penultimateScope = aScope;
        while ( penultimateScope.getParentScope()!=null &&
                penultimateScope.getParentScope().getParentScope() != null ) {
            penultimateScope = penultimateScope.getParentScope();
        }
        return penultimateScope;
    }
    
    public static String toString( Scriptable aScope ) {
        if ( aScope == null ) return "<null>";
        if ( aScope instanceof ScriptableObject ) {
            ScriptableObject sob = (ScriptableObject) aScope;
            return Stream.of(sob.getIds())
                         .map( k -> k.toString() + "=>" + Objects.toString(sob.get(k)) )
                         .collect( joining(",", "{", "}") );
        } else return aScope.toString();
    }
    
    /**
     * Deep-compare of {@code o1} and {@code o2}. Recurses down these objects,
     * when needed.
     *
     * <em>DOES NOT DEAL WITH CIRCULAR REFERENCES!</em>
     *
     * @param o1
     * @param o2
     * @return {@code true} iff both objects are recursively equal
     */
    public static boolean jsEquals(Object o1, Object o2) {
        
        // quick breakers
        if (o1 == o2) return true;
        if (o1 == null ^ o2 == null) return false;
        
        // Can we do a by-part comparison?
        if (o1 instanceof ScriptableObject && o2 instanceof ScriptableObject) {
            return jsScriptableObjectEqual((ScriptableObject) o1, (ScriptableObject) o2);
        }

        // Concatenated strings in Rhino have a different type. We need to manually
        // resolve to String semantics, which is what the following lines do.
        if (o1 instanceof ConsString) {
            o1 = o1.toString();
        }
        if (o2 instanceof ConsString) {
            o2 = o2.toString();
        }
        
        // default comparison (hopefully they have meaningful equals())
        return o1.equals(o2);
    }

    public static boolean jsScriptableObjectEqual(ScriptableObject o1, ScriptableObject o2) {
        Set<Object> o1Ids = new HashSet<>(Arrays.asList(o1.getIds()));
        Set<Object> o2Ids = new HashSet<>(Arrays.asList(o2.getIds()));
        
        return o1Ids.equals(o2Ids) &&
                o1Ids.stream().allMatch(id -> jsEquals(o1.get(id), o2.get(id)));
    }
    
    /**
     * Deep-hash of an object.
     *
     * <em>DOES NOT DEAL WITH CIRCULAR REFERENCES!</em>
     *
     * @param jsObj
     * @return {@code o1}'s hash code.
     */
    public static int jsHash(Object jsObj) {
        if (jsObj == null) {
            return 1;
        }
        
        // Concatenated strings in Rhino have a different type. We need to manually
        // resolve to String semantics, which is what the following lines do.
        if (jsObj instanceof ConsString) {
            return jsObj.toString().hashCode();
        }

        return (jsObj instanceof ScriptableObject)
                ? jsScriptableObjectHashCode((ScriptableObject) jsObj)
                : jsObj.hashCode();
    }

    public static int jsScriptableObjectHashCode(ScriptableObject jsObj) {
        Set<Object> ids = new HashSet<>(Arrays.asList(jsObj.getIds()));
        final int[] acc = new int[1];
        ids.stream().mapToInt( id -> Objects.hash(id, jsObj.get(id)) )
            .forEach( h -> acc[0]^=h );
        
        return acc[0];
    }
}
