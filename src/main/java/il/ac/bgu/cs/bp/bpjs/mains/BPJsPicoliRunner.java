package il.ac.bgu.cs.bp.bpjs.mains;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.LoggingEventSelectionStrategyDecorator;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@CommandLine.Command(name = "BPjs Execution Runtime\n", mixinStandardHelpOptions = true, version = "BPJs version 0.9.1",
header="This program takes a series of BPjs files, and executes them \n" +
        "as a single BProgram.\n",footer="BProgram log and event sequence are written to stdout.")
public class BPJsPicoliRunner implements Runnable {
    @Option(names = { "-v", "--verbose" }, description = "Verbose mode, will log to console steps")
    private boolean verbose = false;

    @Option(names = { "-@", "--stdin" }, description = "Receives input from stdin")
    private boolean stdin = false;

    @Option(names = {"-P", "--param"})
    private Map<String, Object> parameters = new HashMap<>();

    @Parameters(arity = "0..*", paramLabel = "FILE", description = "File(s) to process. At least one")
    private File[] inputFiles;

    public void run() {
        BProgram bpp = new BProgram("BPjs") {
            @Override
            protected void setupProgramScope(Scriptable scope) {
                if (inputFiles != null) {
                    for (File file : inputFiles) {

                        try (InputStream in = Files.newInputStream(file.toPath())) {
                            evaluate(in, file.getName());
                        } catch (EvaluatorException ee) {
                            logScriptExceptionAndQuit(ee, file.getName());
                        } catch (IOException ex) {
                            println("Exception while processing " + file.getName() + ": " + ex.getMessage());
                            Logger.getLogger(BPJsPicoliRunner.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                if (stdin) {
                    println(" [READ] stdin");
                    try {
                        evaluate(System.in, "stdin");
                    } catch (EvaluatorException ee) {
                        logScriptExceptionAndQuit(ee, "stdin");
                    }
                }
            }

            private void logScriptExceptionAndQuit(EvaluatorException ee, String arg) {
                println("Error in source %s:", arg);
                println(ee.details());
                println("line: " + ee.lineNumber() + ":" + ee.columnNumber());
                println("source: " + ee.lineSource());
                System.exit(-3);
            }
        };

        parameters.forEach(bpp::putInGlobalScope);
        SimpleEventSelectionStrategy sess = new SimpleEventSelectionStrategy();
        EventSelectionStrategy ess = verbose ? new LoggingEventSelectionStrategyDecorator(sess) : sess;

        bpp.setEventSelectionStrategy(ess);

        BProgramRunner bpr = new BProgramRunner(bpp);
        if (verbose) {
            bpr.addListener(new PrintBProgramRunnerListener());
        }

        bpr.run();
    }

    public static void main(String[] args) {
        CommandLine.run(new BPJsPicoliRunner(), System.out, args);
    }

    private static void println(String template, String... params) {
        print(template + "\n", params);
    }

    private static void print(String template, String... params) {
        if (params.length == 0) {
            System.out.print("# " + template);
        } else {
            System.out.printf("# " + template, (Object[]) params);
        }
    }
}