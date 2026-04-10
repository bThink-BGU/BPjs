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
public class BpListLog extends AbstractBpLog {

    private final List<String> all   = new LinkedList<>();
    private final List<String> warn  = new LinkedList<>();
    private final List<String> info  = new LinkedList<>();
    private final List<String> fine  = new LinkedList<>();
    private final List<String> off   = new LinkedList<>();
    private final List<String> error = new LinkedList<>();
    
    private PrintStream outStrm = null;

    @Override
    public void doLog(LogLevel lvl, Object msg, Object[] args) {
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
        if ( outStrm != null ) {
            outStrm.println(message);
        }
    }
    
    @Override
    public void setLoggerPrintStream(PrintStream aPrintStream) {
        outStrm = aPrintStream;
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
