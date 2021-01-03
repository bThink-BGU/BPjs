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

import org.junit.Test;
import static org.junit.Assert.*;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

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
        Scriptable aScope = evaluateJS("var a={}; a");
        String expResult = "{JS_Obj }";
        String result = ScriptableUtils.toString(aScope);
        assertEquals(expResult, result);
        
        aScope = evaluateJS("var a={x:\"hello\"}; a");
        expResult = "{JS_Obj x:\"hello\"}";
        result = ScriptableUtils.toString(aScope);
        assertEquals(expResult, result);
        
        aScope = evaluateJS("var a={x:\"hello\", y:\"world\"}; a");
        expResult = "{JS_Obj x:\"hello\", y:\"world\"}";
        result = ScriptableUtils.toString(aScope);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testToStringJSSet() {
        Scriptable aScope = evaluateJS("var a=new Set(); a.add(\"a\"); a.add(\"b\"); a");
        
        String result = ScriptableUtils.toString(aScope);
        assertTrue(result.startsWith("{JS_Set"));
        assertTrue(result.contains("\"a\""));
        assertTrue(result.contains("\"b\""));
        assertTrue(result.contains(", "));
        
    }
    
    
    private Scriptable evaluateJS(String code) {
        try {
            Context curCtx = Context.enter();
            curCtx.setLanguageVersion(Context.VERSION_ES6);
            ImporterTopLevel importer = new ImporterTopLevel(curCtx);
            Scriptable tlScope = curCtx.initStandardObjects(importer);
            Object resultObj = curCtx.evaluateString(
                tlScope, code, 
                "", 1, null);
            
            return (Scriptable)resultObj;
            
        } finally {
            Context.exit();
        }
    }

    /**
     * Test of jsHash method, of class ScriptableUtils.
     */
    @Test
    public void testJsHash() {
        assertEquals( 1, ScriptableUtils.jsHash(null) );
        assertEquals( "test".hashCode(), ScriptableUtils.jsHash("test") );
        ConsString cStr1 = new ConsString("test", "-test");
        assertEquals( "test-test".hashCode(), ScriptableUtils.jsHash(cStr1) );
    }
    
}
