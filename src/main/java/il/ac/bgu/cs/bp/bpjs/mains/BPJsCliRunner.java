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
package il.ac.bgu.cs.bp.bpjs.mains;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.LoggingEventSelectionStrategyDecorator;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;

/**
 * This is a console application for running BPjs files. Source files are passed
 * as arguments at the command line. Program events and log are printed to
 * {@link java.lang.System#out}.
 *
 *
 * @author michael
 */
public class BPJsCliRunner {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsageAndExit();
        }

        BProgram bpp = new BProgram("BPjs") {
            @Override
            protected void setupProgramScope(Scriptable scope) {
                for (String arg : args) {
                    if (arg.equals("-")) {
                        println(" [READ] stdin");
                        try {
                            evaluate(System.in, "stdin");
                        } catch (EvaluatorException ee) {
                            logScriptExceptionAndQuit(ee, arg);
                        }
                    } else {
                        if (!arg.startsWith("-")) {
                            Path inFile = Paths.get(arg);
                            println(" [READ] %s", inFile.toAbsolutePath().toString());
                            if (!Files.exists(inFile)) {
                                println("File %s does not exit", inFile.toAbsolutePath().toString());
                                System.exit(-2);
                            }
                            try (InputStream in = Files.newInputStream(inFile)) {
                                evaluate(in, arg);
                            } catch (EvaluatorException ee) {
                                logScriptExceptionAndQuit(ee, arg);
                            } catch (IOException ex) {
                                println("Exception while processing " + arg + ": " + ex.getMessage());
                                Logger.getLogger(BPJsCliRunner.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    println(" [ OK ] %s", arg);
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

        SimpleEventSelectionStrategy sess = new SimpleEventSelectionStrategy();
        EventSelectionStrategy ess = switchPresent("-v", args) ? new LoggingEventSelectionStrategyDecorator(sess) : sess;

        bpp.setEventSelectionStrategy(ess);

        BProgramRunner bpr = new BProgramRunner(bpp);
        if (!switchPresent("-v", args)) {
            bpr.addListener(new PrintBProgramRunnerListener());
        }

        bpr.run();

    }

    /**
     * @return {@code true} iff the passed switch is present in args.
     */
    private static boolean switchPresent(String aSwitch, String[] args) {
        return Arrays.stream(args).anyMatch(s -> s.trim().equals(aSwitch));
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

    private static void printUsageAndExit() {
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("RunFile-usage.txt")))) {
            rdr.lines().forEach(System.out::println);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot find 'RunFile-usage.txt'");
        }
        System.exit(-1);
    }

}
