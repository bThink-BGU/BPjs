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

import il.ac.bgu.cs.bp.bpjs.BPjs;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

import java.util.stream.IntStream;

import org.mozilla.javascript.*;

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
        return stringify(aScope);
    }
    
    /**
     * Deep-compare of {@code o1} and {@code o2}. Recurses down these objects,
     * when needed.
     *
     * <em>DOES NOT DEAL WITH CIRCULAR REFERENCES!</em>
     *
     * @param o1 first operand for comparison
     * @param o2 second operand for comparison
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
        
        if ( o1 instanceof Map && o2 instanceof Map ) {
            return jsMapEquals((Map)o1, (Map)o2);
        }
        
        if ( o1 instanceof List && o2 instanceof List ) {
            return jsListEquals((List)o1, (List)o2);
        }
        
        if ( o1 instanceof Set && o2 instanceof Set ) {
            return jsSetEquals((Set)o1, (Set)o2);
        }
        
        // Concatenated strings in Rhino are not java.lang.Strings. We need to manually
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
     * An equals method of two maps, that uses JS-equality rather than Java-equality.
     * JS-equality only used for values, not for keys.
     * 
     * @param a A map
     * @param b Another map
     * @return {@code true} iff maps are semantically equal.
     */
    public static boolean jsMapEquals( Map<?,?> a, Map<?,?> b ) {
        if ( a == b ) return true;
        if ( a==null ^ b==null ) return false;
        if ( a.size() != b.size() ) return false;
        if ( ! a.keySet().equals(b.keySet()) ) return false;
        
        return a.keySet().stream().map( k -> jsEquals(a.get(k), b.get(k)) )
            .filter( v -> !v )
            .findAny()
            .isEmpty();
    }
    
    public static boolean jsListEquals( List<?> a, List<?> b ) {
        if ( a == b ) return true;
        if ( a==null ^ b==null ) return false;
        if ( a.size() != b.size() ) return false;
        
        Iterator<?> itA = a.iterator();
        Iterator<?> itB = b.iterator();
        
        while ( itA.hasNext() ) {
            if ( ! jsEquals(itA.next(), itB.next()) ) return false;
        }
        
        return true;
    }
    
    public static boolean jsSetEquals( Set<?> a, Set<?> b ) {
        if ( a == b ) return true;
        if ( a==null ^ b==null ) return false;
        if ( a.size() != b.size() ) return false;
        
        List<?> aAsArr = a.stream().collect( Collectors.toCollection(() -> new ArrayList<>(a.size())));
        
        return b.stream().map(bItm -> 
            aAsArr.stream().filter( aItm -> jsEquals(aItm, bItm) ).findAny() // Maps items from b to whether they are js-present in a
        ).noneMatch(found -> found.isEmpty() );
    }
    
    public static String stringify( Object o ) {
        return stringify(o, false);
    }
    
    public static String stringify( Object o, boolean quoteStrings) {
        if ( o == null ) return "<null>";
        if ( o instanceof String ) return quoteStrings ? "\"" + o + "\"" : (String)o;
        if ( o instanceof ConsString ) return quoteStrings ? "\"" + ((ConsString)o).toString() + "\"" : ((ConsString)o).toString();
        if ( o instanceof NativeArray) {
            NativeArray arr = (NativeArray) o;
            return arr.getIndexIds().stream().map( id -> id + ":" + stringify(arr.get(id), true) ).collect(joining(" | ", "[", "]"));
        }
        if ( o instanceof NativeSet ) {
            NativeSet ns = (NativeSet) o;
            return "Set[" + toString(ns) + "]";
        }
        if ( o instanceof ScriptableObject ) {
            ScriptableObject sob = (ScriptableObject) o;
            return Arrays.stream(sob.getIds()).map( id -> id + ":" + stringify(sob.get(id), true) ).collect( joining(", ", "{", "}"));
        }
        if ( o instanceof Object[] ) {
            Object[] objArr = (Object[]) o;
            return IntStream.range(0, objArr.length).mapToObj(idx -> stringify(objArr[idx], true)).collect(joining(" | ","[J_Array ", "]"));
        }
        if ( o instanceof Map ) {
            Map<?,?> mp = (Map<?,?>) o;
            return mp.entrySet().stream().map( e -> e.getKey()+"->" + stringify(e.getValue(), true)).collect(joining(",","{J_Map ", "}"));
        }
        if ( o instanceof List ) {
            List<?> ls = (List<?>) o;
            return ls.stream().map( e->stringify(e,true) ).collect(joining(", ","<J_List ", ">"));
        }
        if ( o instanceof Set ) {
            Set<?> ls = (Set<?>) o;
            return ls.stream().map( e->stringify(e,true) ).collect(joining(", ","{J_Set ", "}"));
        }
        return o.toString();
        

    }
    
    /**
     * Deep-hash of an object.
     *
     * <em>DOES NOT DEAL WITH CIRCULAR REFERENCES!</em>
     *
     * @param jsObj The JavaScript object to calculate the hash of.
     * @return {@code jsObj}'s hash code.
     */
    public static int jsHashCode(Object jsObj) {
        if (jsObj == null) {
            return 1;
        }
        
        // Concatenated strings in Rhino have a different type. We need to manually
        // resolve to String semantics, which is what the following lines do.
        if (jsObj instanceof ConsString) return jsObj.toString().hashCode();
        if ( jsObj instanceof Map ) return jsMapHashCode( (Map)jsObj );
        if ( jsObj instanceof Collection ) return jsColHashCode( (Collection)jsObj );
        
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
    
    public static int jsMapHashCode( Map<?,?> aMap ) {
        if ( aMap == null ) return 1;
        
        return jsColHashCode( aMap.values() );
    }
    
    public static int jsColHashCode( Iterable<?> aSet ) {
        if ( aSet == null ) return 1;
        
        final int h[] = {0};
        aSet.iterator().forEachRemaining(i->h[0]+=jsHashCode(i) );
        return h[0];    
    }
    
    /**
     * A problematic-yet-working way of getting a meaningful toString 
     * on a NativeSet.
     * @param ns
     * @return a textual description of {@code ns}.
     */
    private static String toString(NativeSet ns) {
        
        String code = "const arr=[]; ns.forEach(e=>arr.push(e)); arr";
        
         try (Context curCtx = BPjs.enterRhinoContext()) {
            Scriptable tlScope = BPjs.makeBPjsSubScope();
            tlScope.put("ns", tlScope, ns);
            Object resultObj = curCtx.evaluateString(
                tlScope, code, 
                "", 1, null);
            
            NativeArray arr = (NativeArray) resultObj;
            return arr.getIndexIds().stream().map( id -> stringify(arr.get(id), true) ).collect(joining(", "));
        }
        
    }
}
