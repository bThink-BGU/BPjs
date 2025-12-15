package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class BpListLog implements BpLog {
        private String level = "info";

        private final List<String> warn= new LinkedList<>();
        private final List<String> info= new LinkedList<>();
        private final List<String> fine= new LinkedList<>();
        private final List<String> off= new LinkedList<>();
        private final List<String> error= new LinkedList<>();

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
            switch (lvl){
                case Warn:
                    warn.add(String.format(msg.toString(), args));
                    break;
                case Info:
                    info.add(String.format(msg.toString(), args));
                    break;
                case Fine:
                    fine.add(String.format(msg.toString(), args));
                    break;
                case Error:
                    error.add(String.format(msg.toString(), args));
                    break;
                case Off:
                default:
                    off.add(String.format(msg.toString(), args));
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
        public void setLoggerPrintStream(PrintStream printStream) {}

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

}
