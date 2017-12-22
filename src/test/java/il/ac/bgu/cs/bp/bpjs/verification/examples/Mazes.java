/*
 * The MIT License
 *
 * Copyright 2017 michael.
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
package il.ac.bgu.cs.bp.bpjs.verification.examples;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.search.FullVisitedNodeStore;
import il.ac.bgu.cs.bp.bpjs.search.Node;
import il.ac.bgu.cs.bp.bpjs.verification.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.verification.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.verification.listeners.BriefPrintDfsVerifierListener;
import il.ac.bgu.cs.bp.bpjs.verification.requirements.EventNotPresent;
import java.util.stream.Collectors;
import org.mozilla.javascript.NativeArray;

/**
 * 
 * This class runs the Mazes(Positive|Negative).js
 * 
 * @author michael
 */
public class Mazes {

//    String implementation = "MazesPositive.js"; 
    String implementation = "MazesNegative.js";
    final BEvent targetFoundEvent = BEvent.named("targetFound");

    // change commented line below to solve a different maze.
//    String mazeName = "trivial";
//      String mazeName = "trivialPlus";
//      String mazeName = "simple";
      String mazeName = "complex";
//      String mazeName = "cow";
//      String mazeName = "singleSolution";
    
    
    public static void main(String[] args) throws InterruptedException {
        Mazes it = new Mazes();
//        it.run();
        it.verify();
    }
    
    public void run() throws InterruptedException {
        SingleResourceBProgram bprog = prepareProgram();
        BProgramRunner rnr = new BProgramRunner(bprog);
        rnr.addListener( new PrintBProgramRunnerListener() );
        rnr.start();
        printMaze(getMaze(bprog));
        
    }
    
    public void verify() throws InterruptedException {
        SingleResourceBProgram bprog = prepareProgram();
        
		try {
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.setRequirement(new EventNotPresent(targetFoundEvent) );
            vfr.setProgressListener( new BriefPrintDfsVerifierListener() );
            vfr.setIterationCountGap(10);
//            vfr.setVisitedNodeStore(new HashVisitedNodeStore());
            vfr.setVisitedNodeStore(new FullVisitedNodeStore());
//            vfr.setVisitedNodeStore(new StateHashVisitedNodeStore());
            final VerificationResult res = vfr.verify(bprog);
            
            char[][] maze = getMaze(bprog);
            printMaze(maze);
            if ( res.isCounterExampleFound() ) {
                System.out.println("Found a counterexample");
                for ( Node nd : res.getCounterExampleTrace() ) {
                    System.out.println(" " + nd.getLastEvent());
                    if ( nd.getLastEvent() != null ) {
                        String name = nd.getLastEvent().getName();
                        if ( name.startsWith("Enter") ) {
                            String loc = name.split("\\(")[1].replace(")","").trim();
                            String coord[] = loc.split(",");
                            int col = Integer.parseInt(coord[0]);
                            int row = Integer.parseInt(coord[1]);
                            maze[row][col]='•';
                        }
                    }
                }
                printMaze(maze);
                
            } else {
                System.out.println("No counterexample found.");
            }
            System.out.printf("Scanned %,d states\n", res.getScannedStatesCount() );
            System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies() );
            
		} catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
	}

    private SingleResourceBProgram prepareProgram() {
        // Create a program
        final SingleResourceBProgram bprog = new SingleResourceBProgram(implementation);
        bprog.putInGlobalScope("MAZE_NAME", mazeName);
        bprog.putInGlobalScope("TARGET_FOUND_EVENT", targetFoundEvent);
        return bprog;
    }
    
    char[][] getMaze( BProgram bprog ) {
        NativeArray jsMaze = bprog.getFromGlobalScope(mazeName, NativeArray.class).get();
        char maze[][] = new char[(int)jsMaze.getLength()][];
        for ( int i=0; i<jsMaze.getLength(); i++ ) {
            maze[i] = jsMaze.get(i).toString().toCharArray();
        }
        
        return maze;
    }
    
    private static void printMaze(char[][] maze) {
        String sep = new String(maze[0]).chars().mapToObj(c->"-").collect( Collectors.joining("+", "+", "+"));
        for ( char[] row : maze ) {
            System.out.println(sep);
            System.out.println(
               new String(row).chars()
                       .mapToObj(c -> new String( new char[]{(char)c}))
                       .collect( Collectors.joining("|", "|", "|"))
            );
        }
        System.out.println(sep);
    }
}
