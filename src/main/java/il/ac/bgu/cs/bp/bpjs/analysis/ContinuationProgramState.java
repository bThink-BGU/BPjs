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
package il.ac.bgu.cs.bp.bpjs.analysis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeContinuation;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

/**
 * Holds data required to determine whether two continuations are the same.
 * Content:
 * <ul>
 *   <li>Program Counter</li>
 *   <li>Stack height</li>
 *   <li>Mapping from variable names to their values.</li>
 * </ul>
 * 
 * Implementation Note: Gathering the data for this class is done in a very
 * hackish way, using shameless reflection. This might and probably will break
 * if Rhino changes their code, but it's the best we can do at this point.
 * 
 * @author michael
 */
public class ContinuationProgramState {
    private final Map<Object, Object> variables = new HashMap<>();
    private int programCounter = -1;
    private int frameIndex = -1;

    public ContinuationProgramState( NativeContinuation nc ) {
        collectStatus(nc.getImplementation());
        collectScopeValues(nc);
    }
    
    private void collectScopeValues(NativeContinuation nc ){
        ScriptableObject current = nc;
        ScriptableObject currentScope = nc;
        try {
            Context.enter();
            while ( current != null ) {
                for ( Object o : current.getIds() ) {
                    if ( !variables.containsKey(o) && o != "bp" ) {
                        Object variableContent = current.get(o);
                        if ( variableContent instanceof Undefined ) continue;
                        variables.put(o, collectJsValue(variableContent));
                    }
                }
                if ( current.getPrototype() != null ) {
                    // advance along the prototype chain
                    current = (ScriptableObject) current.getPrototype();
                } else {
                    // advance the scope chain
                    current = (ScriptableObject) currentScope.getParentScope();
                    currentScope = current;
                }
            }
        } finally {
            Context.exit();
        }
    }
    
    private void collectStatus( Object stackFrame ) {
        try {            
            // get Program counter and frame stack data.
            programCounter = (int) getValue(stackFrame, "pc");
            
            frameIndex = (int) getValue(stackFrame, "frameIndex");
            
            // Grab the variables defined on the stack.
            //  - 1: Get the frame stack variables. They come in two arrays: for objects, and for doubles.
            Object varSource = getValue(stackFrame, "varSource");
            Object[] objectsStack = (Object[]) getValue(varSource, "stack");            
            double[] doublesStack = (double[]) getValue(varSource, "sDbl");
            
            //  - 2: Get the stack variable names
            Object iData = getValue(stackFrame, "idata");
            String[] argNames = (String[]) getValue(iData, "argNames");
            
            //  - 3: Now get the correct value for the name.
            for ( int i=0; i<argNames.length; i++ ) {
                if ( objectsStack[i] instanceof Undefined ) continue;
                Object variableContent = objectsStack[i]==UniqueTag.DOUBLE_MARK ? doublesStack[i] : objectsStack[i];
                variables.put(argNames[i], collectJsValue(variableContent) );
            }
                        
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException ex) {
            throw new RuntimeException("Error extracting field values from Rhino stack: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Take a Javascript value from Rhino, build a Java value for it.
     * @param jsValue
     * @return 
     */
    private Object collectJsValue(Object jsValue) {
        if ( jsValue == null ) {
             return null;
             
        } else if ( jsValue instanceof NativeFunction ) {
            return ((NativeFunction)jsValue).getEncodedSource();
            
        } else if ( jsValue instanceof NativeArray ) {
            NativeArray jsArr = (NativeArray) jsValue;
            List<Object> retVal = new ArrayList<>((int)jsArr.getLength());
            for ( int idx=0; idx<jsArr.getLength(); idx++ ) {
                retVal.add( collectJsValue(jsArr.get(idx)) );
            }
            return retVal;
            
        } else if ( jsValue instanceof ScriptableObject ) {
            ScriptableObject jsObj = (ScriptableObject) jsValue;
            Map<Object, Object> retVal = new HashMap<>();
            for ( Object key:jsObj.getIds() ) {
                retVal.put(key, collectJsValue(jsObj.get(key)) );
            }
            return retVal;
            
        } else if ( jsValue instanceof ConsString ) {
            return ((ConsString)jsValue).toString();
            
        } else if ( jsValue instanceof NativeJavaObject ) {
            NativeJavaObject jsJavaObj = (NativeJavaObject) jsValue;
            Object obj = jsJavaObj.unwrap();
            return obj;
            
        } else {
            String cn = jsValue.getClass().getCanonicalName();
            if ( !cn.startsWith("java.") && (!cn.startsWith("il.ac.bgu")) ) {
                System.out.println("collectJsValue: blind translation to java: " + jsValue + " (" + jsValue.getClass() + ")");
            }
            return jsValue;
        }
        
    }
    
    private Object getValue( Object instance, String fieldName ) throws NoSuchFieldException, IllegalAccessException {
        Field fld = instance.getClass().getDeclaredField(fieldName);
        fld.setAccessible(true);
        return fld.get(instance);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + programCounter;
        hash = 31 * hash + frameIndex;
        hash = 31 * hash + variables.hashCode();
        return hash;
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
        final ContinuationProgramState other = (ContinuationProgramState) obj;
        if (this.programCounter != other.programCounter) {
            return false;
        }
        if (this.frameIndex != other.frameIndex) {
            return false;
        }
        return Objects.equals(this.variables, other.variables);
    }
    
    @Override
    public String toString() {
        return "[ContinuationProgramState pc:" + programCounter + " stackHeight:" + frameIndex + " vars:" + variables + ']';
    }

    public Map<Object, Object> getVisibleVariables() {
        return variables;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public int getFrameIndex() {
        return frameIndex;
    }
    
}
