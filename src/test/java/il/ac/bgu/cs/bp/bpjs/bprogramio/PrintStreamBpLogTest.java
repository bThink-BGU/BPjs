package il.ac.bgu.cs.bp.bpjs.bprogramio;

import java.io.PrintStream;
import java.io.PrintWriter;

import il.ac.bgu.cs.bp.bpjs.bprogramio.log.PrintStreamBpLog;
import org.junit.Test;

public class PrintStreamBpLogTest {

    @Test
    public void defaultConstructorDoesNotThrow() {
        PrintStreamBpLog log = new PrintStreamBpLog();
        log.info("Hello from default constructor");
    }

    @Test
    public void nullPrintWriterConstructorDoesNotThrow() {
        PrintStreamBpLog log = new PrintStreamBpLog((PrintWriter) null);
        log.warn("Hello with null PrintWriter");
    }

    @Test
    public void nullPrintStreamConstructorDoesNotThrow() {
        PrintStreamBpLog log = new PrintStreamBpLog((PrintStream) null);
        log.error("Hello with null PrintStream");
    }

    @Test
    public void setLoggerPrintStreamWithNullDoesNotThrow() {
        PrintStreamBpLog log = new PrintStreamBpLog();
        log.setLoggerPrintStream(null);
        log.info("Hello after setting null PrintStream");
    }
}
