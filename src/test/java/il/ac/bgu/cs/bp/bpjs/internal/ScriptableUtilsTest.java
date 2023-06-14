/*
 * The MIT License
 *
 * Copyright 2019 michael.
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

import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Scriptable;
import static il.ac.bgu.cs.bp.bpjs.TestUtils.*;
import java.util.List;
import java.util.Set;

/**
 *
 * @author michael
 */
public class ScriptableUtilsTest {


    /**
     * Test of toString method, of class ScriptableUtils.
     */
    @Test
    public void testToStringJSObject() {
        Scriptable aScope = (Scriptable)evaluateJS("var a={}; a");
        String expResult = "{}";
        String result = ScriptableUtils.toString(aScope);
        assertEquals(expResult, result);
        
        aScope = (Scriptable)evaluateJS("var a={x:\"hello\"}; a");
        expResult = "{x:\"hello\"}";
        result = ScriptableUtils.toString(aScope);
        assertEquals(expResult, result);
        
        aScope = (Scriptable)evaluateJS("var a={x:\"hello\", y:\"world\"}; a");
        expResult = "{x:\"hello\", y:\"world\"}";
        result = ScriptableUtils.toString(aScope);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testToStringJSSet() {
        Object aScope = evaluateJS("var a=new Set(); a.add(\"a\"); a.add(\"b\"); a");
        
        String result = ScriptableUtils.toString((Scriptable)aScope);
        assertTrue(result.startsWith("Set["));
        assertTrue(result.contains("\"a\""));
        assertTrue(result.contains("\"b\""));
        assertTrue(result.contains(", "));
        
    }
   
    @Test
    public void testToStringJArray() {
        Object arr = new Object[]{"a","b","c",5};
        
        String result = ScriptableUtils.stringify(arr);
        assertTrue(result.startsWith("[J_Array"));
        assertTrue(result.contains("\"a\""));
        assertTrue(result.contains("\"b\""));
        assertTrue(result.contains("\"c\""));
        assertTrue(result.contains(" | "));
        assertTrue(result.contains("5"));
        
    }
    
    @Test
    public void testJsEquals_primitives() {
        Object js1  = evaluateJS("1");
        Object js1B = evaluateJS("1");
        Object js2  = evaluateJS("2");
        Object jsOne  = evaluateJS("\"one\"");
        Object jsOneB = evaluateJS("\"one\"");
        Object jsTwo  = evaluateJS("\"two\"");
        Object jsTrue  = evaluateJS("true");
        Object jsTrueB = evaluateJS("true");
        Object jsFalse = evaluateJS("false");
        
        assertTrue( ScriptableUtils.jsEquals(js1, js1B) );
        assertFalse( ScriptableUtils.jsEquals(js1, js2) ); 
        assertFalse( ScriptableUtils.jsEquals(js1, jsOne) );
        
        assertTrue( ScriptableUtils.jsEquals(jsOne, jsOneB) );
        assertFalse( ScriptableUtils.jsEquals(jsOne, jsTwo) );
        assertFalse( ScriptableUtils.jsEquals(jsOne, js2) );
        
        assertTrue( ScriptableUtils.jsEquals(jsTrue, jsTrueB) );
        assertFalse( ScriptableUtils.jsEquals(jsTrue, jsFalse) );
        assertFalse( ScriptableUtils.jsEquals(jsFalse, js2) );
    }
    
    
    @Test
    public void testJsEquals_simpleObjects() {
        Object jsA = evaluateJS("var a={x:1,y:2,z:'three'}; a");
        Object jsB = evaluateJS("var b={x:1,y:2,z:'three'}; b");
        Object jsC = evaluateJS("var b={x:1,y:2,z:3}; b");
        
        assertTrue( ScriptableUtils.jsEquals(jsA, jsB) );
        assertTrue( ScriptableUtils.jsEquals(jsA, jsA) );
        assertTrue( ScriptableUtils.jsEquals(null, null) );
        assertFalse( ScriptableUtils.jsEquals(jsC, jsB) );
        assertFalse( ScriptableUtils.jsEquals(null, jsB) );
        
        Object jsArrA  = evaluateJS("var b=[1,2,3,\"four\"]; b");
        Object jsArrAb = evaluateJS("var b=[1,2,3,\"four\"]; b");
        Object jsArrB  = evaluateJS("var b=[1,2,9999,\"four\"]; b");
        
        assertTrue( ScriptableUtils.jsEquals(jsArrA, jsArrA) );
        assertTrue( ScriptableUtils.jsEquals(jsArrA, jsArrAb) );
        assertFalse( ScriptableUtils.jsEquals(jsArrA, jsArrB) );    
    }
    
    @Test
    public void testJsEquals_compoundObjects() {
        Object jsA  = evaluateJS("var a={arr:[1,2,3,\"four\"],obj:{a:1, b:2, c:{d:\"three\"}},prim:'three'}; a");
        Object jsAb = evaluateJS("var a={arr:[1,2,3,\"four\"],obj:{a:1, b:2, c:{d:\"three\"}},prim:'three'}; a");
        Object jsC  = evaluateJS("var a={arr:[1,2,3,\"four\"],obj:{a:\"XXX\", b:2, c:{d:\"three\"}},prim:'three'}; a");
        
        assertTrue( ScriptableUtils.jsEquals(jsA, jsA) );
        assertTrue( ScriptableUtils.jsEquals(jsA, jsAb) );
        assertFalse( ScriptableUtils.jsEquals(jsA, jsC) );    
        
        assertTrue( ScriptableUtils.jsEquals(jsExp("[1,2,3,4]"), jsExp("[1,2,3,4]")) );
        assertFalse( ScriptableUtils.jsEquals(jsExp("[1,2,3]"), jsExp("[1,2,3,4]")) );
        assertFalse( ScriptableUtils.jsEquals(jsExp("[1,1,3,4]"), jsExp("[1,2,3,4]")) );
    }
    
    @Test 
    public void testJsEquals_Map() {
        Map<?,?> mpA = Map.of( "a",jsExp("{a:1,b:2,c:3}"),
            "b",jsExp("{a:'a',b:'b',c:['c']}"),
            "c",jsExp("[42, 'digger', 'zaxxon']")
            );
        Map<?,?> mpAb = Map.of( "a",jsExp("{a:1,b:2,c:3}"),
            "b",jsExp("{a:'a',b:'b',c:['c']}"),
            "c",jsExp("[42, 'digger', 'zaxxon']")
        );
        Map<?,?> mpAdim = Map.of( "a",jsExp("{a:1,b:2,c:3}"),
            "c",jsExp("[42, 'digger', 'zaxxon']")
        );
        Map<?,?> mpB = Map.of( "a",jsExp("{a:1,b:2,c:3}"),
            "b",jsExp("{a:'a',b:'b',c:['c']}"),
            "c",jsExp("[42, 'dark castle', 'kings quest']")
        );
        
        assertTrue( ScriptableUtils.jsEquals(mpA, mpAb));
        assertTrue( ScriptableUtils.jsEquals(mpA, mpA));
        assertTrue( ScriptableUtils.jsEquals(null, null));
        
        assertFalse( ScriptableUtils.jsEquals(mpA, null));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpAdim));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpB));
    }
    
    @Test 
    public void testJsEquals_List() {
        List<?> mpA = List.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("{a:'a',b:'b',c:['c']}"),
            jsExp("[42, 'digger', 'zaxxon']")
        );
        List<?> mpAb = List.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("{a:'a',b:'b',c:['c']}"),
            jsExp("[42, 'digger', 'zaxxon']")
        );
        List<?> mpAdim = List.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("[42, 'digger', 'zaxxon']")
        );
        List<?> mpB = List.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("{a:'a',b:'b',c:['c']}"),
            jsExp("[42, 'dark castle', 'kings quest']")
        );
        List<?> mpC = List.of(
            jsExp("{a:1,b:2,c:3}"),
            "I'm a Java String!",
            jsExp("[42, 'dark castle', 'kings quest']")
        );
        
        assertTrue( ScriptableUtils.jsEquals(mpA, mpAb));
        assertTrue( ScriptableUtils.jsEquals(mpA, mpA));
        assertTrue( ScriptableUtils.jsEquals(null, null));
        
        assertFalse( ScriptableUtils.jsEquals(mpA, null));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpAdim));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpB));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpC));
    }
    
    @Test 
    public void testJsEquals_Set() {
        Set<?> mpA = Set.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("{a:'a',b:'b',c:['c']}"),
            jsExp("[42, 'digger', 'zaxxon']")
        );
        Set<?> mpAb = Set.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("{a:'a',b:'b',c:['c']}"),
            jsExp("[42, 'digger', 'zaxxon']")
        );
        Set<?> mpAdim = Set.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("[42, 'digger', 'zaxxon']")
        );
        Set<?> mpB = Set.of(
            jsExp("{a:1,b:2,c:3}"),
            jsExp("{a:'a',b:'b',c:['c']}"),
            jsExp("[42, 'dark castle', 'kings quest']")
        );
        Set<?> mpC = Set.of(
            jsExp("{a:1,b:2,c:3}"),
            "I'm a Java String!",
            jsExp("[42, 'dark castle', 'kings quest']")
        );
        
        assertTrue( ScriptableUtils.jsEquals(mpA, mpAb));
        assertTrue( ScriptableUtils.jsEquals(mpA, mpA));
        assertTrue( ScriptableUtils.jsEquals(null, null));
        
        assertFalse( ScriptableUtils.jsEquals(mpA, null));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpAdim));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpB));
        assertFalse( ScriptableUtils.jsEquals(mpA, mpC));
    }
    

    @Test
    public void testJsHash_simple() {
        assertEquals( 1, ScriptableUtils.jsHashCode(null) );
        assertEquals( "test".hashCode(), ScriptableUtils.jsHashCode("test") );
        ConsString cStr1 = new ConsString("test", "-test");
        assertEquals( "test-test".hashCode(), ScriptableUtils.jsHashCode(cStr1) );
        assertEquals( ScriptableUtils.jsHashCode(jsExp("\"hello\"")), ScriptableUtils.jsHashCode(jsExp("\"hello\"")) );
        assertEquals( ScriptableUtils.jsHashCode(jsExp("[1,2,3]")), ScriptableUtils.jsHashCode(jsExp("[1,2,3]")) );
        assertEquals( ScriptableUtils.jsHashCode(jsExp("{a:1, b:2, c:[3,4,5]}")), ScriptableUtils.jsHashCode(jsExp("{a:1, b:2, c:[3,4,5]}")) );
    }
    
    @Test
    public void testJsHash_maps() {
        assertEquals( 
            ScriptableUtils.jsHashCode(
                Map.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            ),
            ScriptableUtils.jsHashCode(
                Map.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            )
        );
        assertNotEquals( 
            ScriptableUtils.jsHashCode(
                Map.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"), "r", 9000) // the 9000 bosts the hashcode, since the algorithm sums value hashcodes.
            ),
            ScriptableUtils.jsHashCode(
                Map.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            )
        );
    }
    
    @Test
    public void testJsHash_collections() {
        assertEquals( 
            ScriptableUtils.jsHashCode(
                Set.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            ),
            ScriptableUtils.jsHashCode(
                Set.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            )
        );
        
        assertNotEquals( 
            ScriptableUtils.jsHashCode(
                Set.of("X", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            ),
            ScriptableUtils.jsHashCode(
                Set.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            )
        );
        
        assertEquals( 
            ScriptableUtils.jsHashCode(
                List.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            ),
            ScriptableUtils.jsHashCode(
                List.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            )
        );
        
        assertNotEquals( 
            ScriptableUtils.jsHashCode(
                List.of("X", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            ),
            ScriptableUtils.jsHashCode(
                List.of("h", jsExp("\"hello\""), "aNum", jsExp("30.44"), "anObj", jsExp("{a:'a',b:'bee',c:'See!'}"))
            )
        );
    }
    
    
}
