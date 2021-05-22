/*
 * The MIT License
 *
 * Copyright 2020 michael.
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
package il.ac.bgu.cs.bp.bpjs;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsCodeEvaluationException;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class BrokenCodeTest {
    
    @Test(expected=BPjsCodeEvaluationException.class)
    public void brokenReference1stSyncRunTest() {
        BProgram bp = new ResourceBProgram("broken/refError_1stSync.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.setBProgram(bp);
        rnr.run();
    }
    
    @Test(expected=BPjsCodeEvaluationException.class)
    public void brokenReference1stSyncDfsTest() throws Exception {
        BProgram bp = new ResourceBProgram("broken/refError_1stSync.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.verify(bp);
    }
    
    @Test(expected=BPjsCodeEvaluationException.class)
    public void brokenReferenceRunTest() {
        BProgram bp = new ResourceBProgram("broken/refError.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.setBProgram(bp);
        rnr.run();
    }
    
    @Test(expected=BPjsCodeEvaluationException.class)
    public void brokenReferenceDfsTest() throws Exception {
        BProgram bp = new ResourceBProgram("broken/refError.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        vfr.verify(bp);
    }
    
    
    @Test(expected=BPjsCodeEvaluationException.class)
    public void brokenSyntaxRunTest() throws IOException {
        try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("broken/syntaxError.js")) {
            String raw = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            String post = Arrays.asList( raw.split("\n") ).stream().filter(s->!s.contains("REMOVE")).collect(joining("\n"));
            BProgram bp = new StringBProgram(post);
            BProgramRunner rnr = new BProgramRunner();
            rnr.setBProgram(bp);
            rnr.run();
        }
    }
    
    
    @Test(expected=BPjsCodeEvaluationException.class)
    public void brokenSyntaxDfsTest() throws Exception {
        try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream("broken/syntaxError.js")) {
            String raw = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
            String post = Arrays.asList( raw.split("\n") ).stream().filter(s->!s.contains("REMOVE")).collect(joining("\n"));
            BProgram bp = new StringBProgram(post);
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.verify(bp);
        }
    }
    
    @Test()
    public void requestNonEvent() throws Exception {
        testRuntimeError(1, "NON_EVENT");
    }
    
    @Test()
    public void requestNonEventInArray() throws Exception {
        testRuntimeError(2, "NON_EVENT");
    }
    
    @Test()
    public void waitedForNonEvent() throws Exception {
        testRuntimeError(3, "NON_EVENT_or_SET");
    }
    
    @Test()
    public void blockedForNonEvent() throws Exception {
        testRuntimeError(4, "NON_EVENT_or_SET");
    }
    
    @Test()
    public void interruptingForNonEvent() throws Exception {
        testRuntimeError(5, "NON_EVENT_or_SET");
    }
    
    
    public void testRuntimeError(int swValue, String expectedInReport) throws Exception {
        BProgram bp = new ResourceBProgram("broken/requestNonEvents.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.setBProgram(bp);
        final AtomicBoolean exceptionReported = new AtomicBoolean();
        rnr.addListener(new BProgramRunnerListenerAdapter() {
            @Override
            public void error(BProgram bp, Exception ex) {
                assertTrue("Message expected to contain '" + expectedInReport + "'",ex.getMessage().contains(expectedInReport));
                exceptionReported.set(true);
            }
        });
        bp.putInGlobalScope("sw", swValue);
        rnr.run();
        
        assertTrue(exceptionReported.get());
    }
}
