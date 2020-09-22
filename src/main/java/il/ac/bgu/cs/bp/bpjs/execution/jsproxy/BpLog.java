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
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptableObject;

/**
 * Simple logging mechanism for {@link BProgram}s.
 * 
 * @author michael
 */
public class BpLog implements java.io.Serializable {
    
    public enum LogLevel {
        Off, Warn, Info, Fine
    }
    LogLevel level = LogLevel.Info;

    public void warn(Object msg) {
        log(LogLevel.Warn, msg);
    }

    public void info(Object msg) {
        log(LogLevel.Info, msg);
    }

    public void fine(Object msg) {
        log(LogLevel.Fine, msg);
    }

    public void log(LogLevel lvl, Object msg) {
        if (level.compareTo(lvl) >= 0) {
            System.out.println("[BP][" + lvl.name() + "] " + stringify(msg));
        }
    }

    public void setLevel(String levelName) {
        synchronized (this) {
            level = LogLevel.valueOf(levelName);
        }
    }

    public String getLevel() {
        return level.name();
    }
    
    private String stringify( Object o ) {
        if ( o == null ) return "<null>";
        if ( o instanceof String ) return "\""+o + "\"";
        if ( o instanceof ConsString ) return "\"" + ((ConsString)o).toString() + "\"";
        if ( o instanceof NativeArray ) {
            NativeArray arr = (NativeArray) o;
            return arr.getIndexIds().stream().map( id -> id + ": " + stringify(arr.get(id)) ).collect(joining("|", "[JS_Array ", "]"));
        }
        if ( o instanceof ScriptableObject ) {
            ScriptableObject sob = (ScriptableObject) o;
            return Arrays.stream(sob.getIds()).map( id -> id + ": " + stringify(sob.get(id)) ).collect( joining("\n", "{JS_Obj ", "}"));
        }
        if ( o instanceof Object[] ) {
            Object[] objArr = (Object[]) o;
            return IntStream.range(0, objArr.length).mapToObj(idx -> stringify(objArr[idx])).collect(joining("|","[Array ", "]"));
        }
        if ( o instanceof Map ) {
            Map<?,?> mp = (Map<?,?>) o;
            return mp.entrySet().stream().map( e -> e.getKey()+"->" + stringify(e.getValue())).collect(joining(",","{Map ", "}"));
        }
        if ( o instanceof List ) {
            List<?> ls = (List<?>) o;
            return ls.stream().map(this::stringify).collect(joining(",","<List ", ">"));
        }
        if ( o instanceof Set ) {
            Set<?> ls = (Set<?>) o;
            return ls.stream().map(this::stringify).collect(joining(",","{Set ", "}"));
        }
        return o.toString();
        
    }
    
}
