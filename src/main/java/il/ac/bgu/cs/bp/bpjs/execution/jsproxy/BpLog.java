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

    public void warn(String msg) {
        log(LogLevel.Warn, msg);
    }

    public void info(String msg) {
        log(LogLevel.Info, msg);
    }

    public void fine(String msg) {
        log(LogLevel.Fine, msg);
    }

    public void log(LogLevel lvl, String msg) {
        if (level.compareTo(lvl) >= 0) {
            System.out.println("[BP][" + lvl.name() + "] " + msg);
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
