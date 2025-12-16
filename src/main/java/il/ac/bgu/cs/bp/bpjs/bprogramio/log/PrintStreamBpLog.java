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
package il.ac.bgu.cs.bp.bpjs.bprogramio.log;


import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Logs BPjs messages to a {@link PrintStream} or a {@link PrintWriter}.
 *
 * @author maor
 */
public class PrintStreamBpLog implements BpLog {

    LogLevel level = DEFAULT_LOG_LEVEL;
    private PrintWriter out;
    
    private boolean autoFlush = true;
    
    public PrintStreamBpLog(PrintWriter aWriter ){
        out = aWriter;
    }
    
    public PrintStreamBpLog(PrintStream aStream ){
        this(new PrintWriter(aStream));
    }

    public PrintStreamBpLog() {
        this( System.out );
    }

    @Override
    public void warn(Object msg, Object... args) {
        log(LogLevel.Warn, msg, args);
    }

    @Override
    public void info(Object msg, Object... args) {
        log(LogLevel.Info, msg, args);
    }


    @Override
    public void fine(Object msg, Object... args) {
        log(LogLevel.Fine, msg, args);
    }

    @Override
    public void error(Object msg, Object... args) {
        log(LogLevel.Error, msg, args);
    }

    @Override
    public void log(LogLevel lvl, Object msg, Object[] args) {
        if (level.compareTo(lvl) >= 0) {
            synchronized (this) {
                out.println(formatMessage(lvl, msg, args));
                if ( autoFlush ) {
                    flush();
                }
            }
        }
    }

    @Override
    public void setLevel(String levelName) {
        synchronized (this) {
            try {
                level = LogLevel.valueOf(levelName.trim());
            } catch (IllegalArgumentException iae) {
                error("Unknown log level: '{0}'", levelName);
            }
        }
    }

    @Override
    public String getLevel() {
        return level.name();
    }

    public LogLevel getTypedLevel() {
        return level;
    }
    

    @Override
    public void setLoggerPrintStream(PrintStream printStream){
        out = new PrintWriter(printStream);
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }
    
    public void flush() {
        out.flush();
    }
    
}
