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

import il.ac.bgu.cs.bp.bpjs.analysis.*;
import il.ac.bgu.cs.bp.bpjs.analysis.listeners.PrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozilla.javascript.Context;
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
public class BPjsCliRunner {

    public static void main(String[] args) throws Exception {
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
                            evaluate(System.in, "stdin", Context.getCurrentContext());
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
                                evaluate(in, arg, Context.getCurrentContext());
                            } catch (EvaluatorException ee) {
                                logScriptExceptionAndQuit(ee, arg);
                            } catch (IOException ex) {
                                println("Exception while processing " + arg + ": " + ex.getMessage());
                                Logger.getLogger(BPjsCliRunner.class.getName()).log(Level.SEVERE, null, ex);
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
        
        if ( switchPresent("--verify", args) ) {
            bpp.setEventSelectionStrategy(sess);
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.setDebugMode( switchPresent("-v", args));
            vfr.setProgressListener(new PrintDfsVerifierListener() );
            
            if ( switchPresent("--full-state-storage", args) ) {
                println("Using full state storage");
                vfr.setVisitedStateStore( new BThreadSnapshotVisitedStateStore() );
            } else {
                vfr.setVisitedStateStore( new BProgramSnapshotVisitedStateStore() );
            }
            if ( switchPresent("--liveness", args) ) {
                vfr.addInspection(ExecutionTraceInspections.HOT_SYSTEM);
                vfr.addInspection(ExecutionTraceInspections.HOT_BTHREADS);
                vfr.addInspection(ExecutionTraceInspections.HOT_TERMINATIONS);
            }
            String maxDepthStr = keyForValue("--max-trace-length", args);
            if ( maxDepthStr != null ) {
                try {
                    long maxDepth = Long.parseLong(maxDepthStr.trim());
                    vfr.setMaxTraceLength(maxDepth);
                } catch ( NumberFormatException nfe ) {
                    println("Illegal max trace length value: '" + maxDepthStr + "'.");
                    System.exit(-5);
                }
            }
            println("Max trace length: " + vfr.getMaxTraceLength() );
            
            if ( vfr.getInspections().isEmpty() ) {
                ExecutionTraceInspections.DEFAULT_SET.forEach(vfr::addInspection);
            }
            println("Inspections:");
            vfr.getInspections().forEach( ins -> println( " * " + ins.title()));
            
            try {
                println("Starting verification");
                VerificationResult res = vfr.verify(bpp);
                println("Verification completed.");
                
                if ( res.getViolation().isPresent() ) {
                    Violation vio = res.getViolation().get();
                    println("Found Violation:");
                    println(vio.decsribe());
                    
                    println("Counter example trace:");
                    vio.getCounterExampleTrace().getNodes().stream()
                        .map( n -> n.getEvent() )
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(evt -> println(evt.toString()));

                } else {
                    println("No violations found");
                }
                
                println("General statistics:");
                println(String.format("Time:\t%,d (msec)", res.getTimeMillies()));
                println(String.format("States scanned:\t%,d", res.getScannedStatesCount()));
                println(String.format("Edges scanned:\t%,d", res.getScannedEdgesCount()));
                                    
                
            } catch ( Exception e ) {
                println("!! Exception during verifying the program: " + e.getMessage());
                println("!! Stack trace:");
                e.printStackTrace(System.out);
            }
            
        } else {
            EventSelectionStrategy ess = switchPresent("-v", args) ? new LoggingEventSelectionStrategyDecorator(sess) : sess;

            bpp.setEventSelectionStrategy(ess);

            BProgramRunner bpr = new BProgramRunner(bpp);
            if (!switchPresent("-v", args)) {
                bpr.addListener(new PrintBProgramRunnerListener());
            }

            bpr.run();            
        }
    }

    /**
     * @return {@code true} iff the passed switch is present in args.
     */
    private static boolean switchPresent(String aSwitch, String[] args) {
        return Arrays.stream(args).anyMatch(s -> s.trim().equals(aSwitch));
    }
    
    private static String keyForValue( String aKey, String args[] ) {
       for ( int idx=0; idx<args.length; idx++ ) {
           if ( args[idx].startsWith(aKey+"=") ) {
               String[] comps = args[idx].split("=", 2);
               return comps.length == 2 ? comps[1] : null;
           }
       }
       return null;
    }
    
    private static void println(String template, String... params) {
        print(template + "\n", params);
    }

    private static void print(String template, String... params) {
        if (params.length == 0) {
            System.out.print(template);
        } else {
            System.out.printf(template, (Object[]) params);
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
