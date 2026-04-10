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

import il.ac.bgu.cs.bp.bpjs.internal.ScriptableUtils;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

/**
 * Base interface for loggers in the BPjs system. 
 * 
 * @author michael
 */
public interface BpLog extends java.io.Serializable {
    LogLevel DEFAULT_LOG_LEVEL = LogLevel.Info;

    void warn(Object msg, Object... args);

    void info(Object msg, Object... args);

    void fine(Object msg, Object... args);

    void error(Object msg, Object... args);

    void log(LogLevel lvl, Object msg, Object[] args);

    void setLevel(String levelName);

    String getLevel();
    
    boolean isLevelEnabled(String levelName);
    
    void setLoggerPrintStream(PrintStream printStream);

    public enum LogLevel {
        Off, Error, Warn, Info, Fine;
        
        public static LogLevel lenientValueOf(String levelName) {
            if ( levelName == null ) return LogLevel.Info;
            if ( levelName.equals("") ) return LogLevel.Info;
            
            String lcLevelName = levelName.trim().toLowerCase();
            for( LogLevel l : LogLevel.values() ){
                if (l.toString().toLowerCase().equals(lcLevelName) ) {
                    return l;
                }
            }
            
            throw new IllegalArgumentException("Unknown log level: '" + levelName + "'");
        }
    }
    
    /**
     * A set of classes that can use their own {@code toString()} method. Classes 
     * not in this set are formatted using custom formatting code.
     */
    public static final Set<Class<?>> PASS_THROUGH = Set.of(Integer.class, Long.class,
        Short.class, Double.class, Float.class, String.class, Date.class, LocalDate.class, LocalDateTime.class);
    
    /**
     * Default formatting of a log message. This method should not deal with 
     * log-level filtering, only with string formatting.
     * Formatting is done using the {@link MessageFormat} class.
     * @param lvl Logging level.
     * @param msg The message template. A toString()-able object.
     * @param args Template arguments.
     * @return Formatted string.
     * @see MessageFormat
     */
    default String formatMessage(LogLevel lvl, Object msg, Object[] args) {
        String prefix = "[BP][" + lvl.name() + "] ";
        if ( msg == null ) return prefix + "<null>";
        if ( args.length == 0 ) { 
            return prefix + formatArg(msg);
        }
        try {
            Object[] formattedArgs = Arrays.stream(args).map(this::formatArg).toArray();
            return prefix + MessageFormat.format( formatArg(msg).toString(), formattedArgs );
        } catch (Exception e) {
            error( "Message formatting exception: " + e.getMessage() );
            return prefix + msg;
        }   
    }
    
    /**
     * Formats a single logging message argument. NOTE: for pass-through classes, 
     * the formatted argument needs to maintain its class, so that the MessageFormat
     * instructions can kick in.
     * 
     * @param arg an object to format
     * @return a {@code toString()}-able view of {@code arg}.
     */
    default Object formatArg(Object arg) {
        if ( arg == null ) return null;
        if ( PASS_THROUGH.contains(arg.getClass()) ) return arg;
        return ScriptableUtils.stringify(arg);
    }

}
