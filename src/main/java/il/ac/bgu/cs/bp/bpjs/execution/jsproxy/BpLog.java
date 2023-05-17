package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import java.io.PrintStream;

public interface BpLog extends java.io.Serializable {
    LogLevel DEFAULT_LOG_LEVEL = LogLevel.Info;

    void warn(Object msg, Object... args);

    void info(Object msg, Object... args);

    void fine(Object msg, Object... args);

    void log(LogLevel lvl, Object msg, Object[] args);

    void setLevel(String levelName);

    String getLevel();

    void setLoggerPrintStream(PrintStream printStream);

    public enum LogLevel {
        Off, Warn, Info, Fine
    }
}
