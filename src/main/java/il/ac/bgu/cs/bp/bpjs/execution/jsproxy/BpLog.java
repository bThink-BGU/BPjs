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

import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.text.MessageFormat;
import java.util.Arrays;

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

    public void warn(Object msg, Object ...args) {
        log(LogLevel.Warn, msg, args);
    }

    public void info(Object msg, Object ...args) {
        log(LogLevel.Info, msg, args);
    }

    public void fine(Object msg, Object ...args) {
        log(LogLevel.Fine, msg, args);
    }

    public void log(LogLevel lvl, Object msg, Object... args) {
        if (level.compareTo(lvl) >= 0) {
            System.out.println("[BP][" + lvl.name() + "] " +
                args == null || args.length == 0 ? ScriptableUtils.stringify(msg) :
                MessageFormat.format(ScriptableUtils.stringify(msg), Arrays.stream(args).map(ScriptableUtils::stringify).toArray()));
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
    
}
