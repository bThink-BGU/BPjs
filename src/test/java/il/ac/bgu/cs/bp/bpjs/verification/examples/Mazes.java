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

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.search.HashVisitedNodeStore;
import il.ac.bgu.cs.bp.bpjs.search.Node;
import il.ac.bgu.cs.bp.bpjs.verification.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.verification.VerificationResult;
import java.util.stream.Collectors;
import org.mozilla.javascript.NativeArray;

/**
 * 
 * This class runs the mazes.js
 * 
 * @author michael
 */
public class Mazes {

    public static void main(String[] args) throws InterruptedException {
		// Create a program
		final SingleResourceBProgram bprog = new SingleResourceBProgram("Mazes.js");
//        String mazeName = "trivial";
//        String mazeName = "trivialPlus";
//        String mazeName = "complex";
        String mazeName = "singleSolution";
        bprog.putInGlobalScope("MAZE_NAME", mazeName);

//        BProgramRunner rnr = new BProgramRunner(bprog);
//        rnr.addListener( new StreamLoggerListener() );
//        rnr.start();
//
//        NativeArray jsMaze = bprog.getFromGlobalScope(mazeName, NativeArray.class).get();
//        char maze[][] = new char[(int)jsMaze.getLength()][];
//        for ( int i=0; i<jsMaze.getLength(); i++ ) {
//            maze[i] = jsMaze.get(i).toString().toCharArray();
//        }
//        printMaze(maze);
        
		try {
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
//            vfr.setVisitedNodeStore(new FullVisitedNodeStore());
            vfr.setVisitedNodeStore(new HashVisitedNodeStore());
//            vfr.setVisitedNodeStore(new StateHashVisitedNodeStore());
            final VerificationResult res = vfr.verify(bprog);
            
            NativeArray jsMaze = bprog.getFromGlobalScope(mazeName, NativeArray.class).get();
            char maze[][] = new char[(int)jsMaze.getLength()][];
            for ( int i=0; i<jsMaze.getLength(); i++ ) {
                maze[i] = jsMaze.get(i).toString().toCharArray();
            }
            printMaze(maze);

            
            if ( res.isCounterExampleFound() ) {
                System.out.println("Found a counterexample");
                for ( Node nd : res.getCounterExampleTrace() ) {
                    System.out.println(" " + nd.getLastEvent());
                    if ( nd.getLastEvent() != null ) {
                        String name = nd.getLastEvent().getName();
                        String loc = name.split("\\(")[1].replace(")","").trim();
                        String coord[] = loc.split(",");
                        int col = Integer.parseInt(coord[0]);
                        int row = Integer.parseInt(coord[1]);
                        maze[row][col]='•';
                    }
                }
                printMaze(maze);
                
            } else {
                System.out.println("No counterexample found.");
            }
            System.out.printf("Scanned %,d states\n", res.getStatesScanned() );
            System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies() );
            
		} catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
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
