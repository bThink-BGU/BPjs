package il.ac.bgu.cs.bp.bpjs.bprogramio.log;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * An in-memory BP logger that logs all messages to lists. These can be
 * inspected later, e.g. for testing.
 *
 * @author michael
 */
public class BpListLog implements BpLog {

    private String level = "info";

    private final List<String> all   = new LinkedList<>();
    private final List<String> warn  = new LinkedList<>();
    private final List<String> info  = new LinkedList<>();
    private final List<String> fine  = new LinkedList<>();
    private final List<String> off   = new LinkedList<>();
    private final List<String> error = new LinkedList<>();

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
        String message = formatMessage(lvl, msg, args);
        all.add(message);
        switch (lvl) {
            case Warn:
                warn.add(message);
                break;
            case Info:
                info.add(message);
                break;
            case Fine:
                fine.add(message);
                break;
            case Error:
                error.add(message);
                break;
            case Off:
            default:
                off.add(message);
                break;
        }
    }

    @Override
    public void setLevel(String levelName) {
        level = levelName;
    }

    @Override
    public String getLevel() {
        return level;
    }

    @Override
    public void setLoggerPrintStream(PrintStream printStream) {
    }

    public List<String> getWarn() {
        return warn;
    }

    public List<String> getInfo() {
        return info;
    }

    public List<String> getFine() {
        return fine;
    }

    public List<String> getError() {
        return error;
    }

    public List<String> getOff() {
        return off;
    }

    public List<String> getAll() {
        return all;
    }

}
