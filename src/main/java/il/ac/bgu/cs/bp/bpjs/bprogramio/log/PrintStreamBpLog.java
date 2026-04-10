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
public class PrintStreamBpLog extends AbstractBpLog {

    private PrintWriter out = new PrintWriter(System.out);
    
    private boolean autoFlush = true;
    
    public PrintStreamBpLog(PrintWriter aWriter ){
        this.out = (aWriter != null) ? aWriter : new PrintWriter(System.out);
    }
    
    public PrintStreamBpLog(PrintStream aStream ){
        this(aStream != null ? new PrintWriter(aStream) : null);
    }

    public PrintStreamBpLog() {
        this( System.out );
    }
    
    @Override
    protected void doLog(LogLevel lvl, Object msg, Object[] args) {
        synchronized (this) {
            out.println(formatMessage(lvl, msg, args));
            if ( autoFlush ) {
                flush();
            }
        }
    }

    @Override
    public void setLoggerPrintStream(PrintStream printStream){
        this.out = (printStream != null)
                ? new PrintWriter(printStream)
                : new PrintWriter(System.out);
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }
    
    public void flush() {
        if(out != null) out.flush();
    }
    
}
