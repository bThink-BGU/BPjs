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
package il.ac.bgu.cs.bp.bpjs.search;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author michael
 */
public class SampleSearch {

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        
        // 1. Setup the program
        BProgram simpleProg = new SingleResourceBProgram("search/SimpleProgramForSearch.js");
        BProgramSyncSnapshot seed = simpleProg.setup().start(); // seed is after BThreads are registered and before they run.
        
        BProgramSyncSnapshotCloner cloner = new BProgramSyncSnapshotCloner();
        
        // three event orders we're about to explore
        List<List<String>> eventOrderings = Arrays.asList( Arrays.asList("A1","A2","B1","B2"),
                                                        Arrays.asList("A1","B1","A2","B2"),
                                                        Arrays.asList("B1","A1","B2","A2") );
        
        // explore each event ordering
        for ( List<String> events : eventOrderings ) {
            System.out.println("Running event set: " + events);
            BProgramSyncSnapshot cur = cloner.clone(seed); // get a fresh copy
            for ( String s : events ) {
                cur = cur.triggerEvent(new BEvent(s));
            }
            System.out.println("..Done");
        }
       

    }
    
}
