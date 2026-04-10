/*
 * The MIT License
 *
 * Copyright 2026 michael.
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

/**
 * Base class for loggers, containing convenience methods.
 * 
 * @author michael
 */
public abstract class AbstractBpLog implements BpLog {
    
    
    protected LogLevel level = DEFAULT_LOG_LEVEL;
    
    @Override
    public void warn(Object aMessage, Object... someArgs) {
        log(LogLevel.Warn, aMessage, someArgs);
    }

    @Override
    public void info(Object aMessage, Object... someArgs) {
        log(LogLevel.Info, aMessage, someArgs);
    }

    @Override
    public void fine(Object aMessage, Object... someArgs) {
        log(LogLevel.Fine, aMessage, someArgs);
    }

    @Override
    public void error(Object aMessage, Object... someArgs) {
        log(LogLevel.Error, aMessage, someArgs);
    }
    
    @Override
    public void log(LogLevel aLevel, Object aMessage, Object[] someArgs) {
        if (level.compareTo(aLevel) >= 0) {
            doLog(aLevel, aMessage, someArgs);
        }
    }
    
    @Override
    public boolean isLevelEnabled(String levelName) {
        LogLevel aLevel = LogLevel.lenientValueOf(levelName.trim());
        return (level.compareTo(aLevel) >= 0);
    }
    
    @Override
    public void setLevel(String levelName) {
        synchronized (this) {
            try {
                level = LogLevel.lenientValueOf(levelName.trim());
            } catch (IllegalArgumentException iae) {
                error("Unknown log level: '{0}'", levelName);
            }
        }
    }

    @Override
    public String getLevel() {
        return getTypedLevel().name();
    }

    public LogLevel getTypedLevel() {
        return level;
    }
    
    public void setTypedLevel( LogLevel aLevel ) {
        level = (aLevel!=null ? aLevel : LogLevel.Info);
    }
    
    /**
     * Actual logging happens here. No need to filter by log level etc.
     * 
     * @param aLevel
     * @param aMessage
     * @param someArgs 
     */
    protected abstract void doLog(LogLevel aLevel, Object aMessage, Object[] someArgs);
}
