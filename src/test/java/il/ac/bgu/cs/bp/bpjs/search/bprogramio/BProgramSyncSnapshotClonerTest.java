package il.ac.bgu.cs.bp.bpjs.search.bprogramio;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.PrintBProgramListener;
import il.ac.bgu.cs.bp.bpjs.search.FullVisitedNodeStore;
import il.ac.bgu.cs.bp.bpjs.verification.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.verification.VerificationResult;
import org.junit.Test;

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

/**
 *
 * @author michael
 */
public class BProgramSyncSnapshotClonerTest {
    
    
    @Test
    public void testProgramIsOk() throws InterruptedException {
        BProgramRunner bpr = new BProgramRunner(new SingleResourceBProgram("BProgramSyncSnapshotClonerTest.js"));
        bpr.addListener(new PrintBProgramListener() );
        InMemoryEventLoggingListener events = bpr.addListener( new InMemoryEventLoggingListener() );
        
        bpr.start();
        
        events.getEvents().forEach( e -> System.out.println(e.toString()) );
        
    }
    
    @Test
    public void testSerialization() throws Exception {
        System.out.println("\nSTART Serialization test");
        BProgram bprog = new SingleResourceBProgram("BProgramSyncSnapshotClonerTest.js");
        BProgramSyncSnapshot cur = bprog.setup();
        cur = cur.start();
        cur.triggerEvent( cur.getStatements().stream().flatMap(s->s.getRequest().stream()).findFirst().get() );
        BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(bprog);
        byte[] out = io.serialize(cur);
        System.out.println("de-serializing\n");
        io.deserialize(out);
        System.out.println("END Serialization test\n");
        
    }
    
    @Test
    public void verifyProgram() throws Exception {
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        BProgram bprog = new SingleResourceBProgram("BProgramSyncSnapshotClonerTest.js");
        vfr.setVisitedNodeStore(new FullVisitedNodeStore());
        final VerificationResult res = vfr.verify(bprog);
        System.out.println("res = " + res.getCounterExampleTrace());
    }
    
}
