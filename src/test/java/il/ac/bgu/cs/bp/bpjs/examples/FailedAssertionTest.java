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
package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class FailedAssertionTest {
    
   
    @Test
    public void testAssertion() throws InterruptedException {
        final SingleResourceBProgram bprog = new SingleResourceBProgram("FailedAssertionTest.js");
        BProgramRunner sut = new BProgramRunner(bprog);
        sut.addListener( new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        System.out.println("About to start program");
        bprog.putInGlobalScope("shouldFail", true);
        sut.run();
        
        assertEquals( Arrays.asList("piff", "puff", "poof!"), eventLogger.eventNames() );
        assertTrue( sut.hasFailedAssertion() );
        FailedAssertion fa = sut.getFailedAssertion();
        assertEquals("assertor", fa.getBThreadName());
        assertEquals("Poof has happened.", fa.getMessage());
        
    }
    
     @Test
    public void testNoAssertion() throws InterruptedException {
        final SingleResourceBProgram bprog = new SingleResourceBProgram("FailedAssertionTest.js");
        BProgramRunner sut = new BProgramRunner(bprog);
        sut.addListener( new PrintBProgramRunnerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        System.out.println("About to start program");
        bprog.putInGlobalScope("shouldFail", false);
        sut.run();
        
        assertEquals( Arrays.asList("piff", "puff", "poof!", "peff"), eventLogger.eventNames() );
        assertFalse( sut.hasFailedAssertion() );
    }
    
}
