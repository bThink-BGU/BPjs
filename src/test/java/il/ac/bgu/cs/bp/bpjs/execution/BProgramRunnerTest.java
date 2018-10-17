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
package il.ac.bgu.cs.bp.bpjs.execution;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author michael
 */
public class BProgramRunnerTest {

    public BProgramRunnerTest() {
    }

    /**
     * Test of start method, of class BProgramRunner.
     */
    @Test
    public void testRun() {

        BProgram bprog = new SingleResourceBProgram("HotNCold.js");
        BProgramRunner sut = new BProgramRunner(bprog);

        sut.addListener(new PrintBProgramRunnerListener());

        sut.run();

    }

    @Test
    public void testImmediateAssert() {
        BProgram bprog = new SingleResourceBProgram( "ImmediateAssert.js");
        BProgramRunner runner = new BProgramRunner(bprog);
        runner.run();
        InMemoryEventLoggingListener listener = new InMemoryEventLoggingListener();
        runner.addListener(listener);
        FailedAssertion expected = new FailedAssertion("failRightAWay!", "forward");
        assertEquals(expected, runner.getFailedAssertion());
        assertEquals(0, listener.getEvents().size());


    }


    @Test
    public void testExecutorName() {
        BProgram bprog = new StringBProgram(
                "var exName = 'initial value'\n"
                        + "bp.registerBThread( function(){\n"
                        + "  exName = bp.getJavaThreadName();\n"
                        + "});"
        );

        new BProgramRunner(bprog).run();
        String exName = bprog.getFromGlobalScope("exName", String.class).get();
        assertTrue("Java executor name is wrong (got:'" + exName + "')",
                exName.startsWith("BProgramRunner-"));
    }
    
    @Test(timeout=3000)
    public void testHalt() {
        BProgram bprog = new StringBProgram(
            "bp.registerBThread(function(){\n" +
            "while (true){\n" +
                "bp.sync({request:bp.Event(\"evt\")});"+
                "}"+
            "});"
        );
        
        BProgramRunner rnr = new BProgramRunner(bprog);
        AtomicBoolean haltedCalled = new AtomicBoolean();
        AtomicBoolean endedCalled = new AtomicBoolean();
        rnr.addListener( new BProgramRunnerListenerAdapter() {
            @Override
            public void halted(BProgram bp) {
                haltedCalled.set(true);
            }

            @Override
            public void ended(BProgram bp) {
                endedCalled.set(true);
            }
        });
        
        new Thread(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {}
            rnr.halt();
        }).start();
        
        rnr.run();
        
        assertTrue( haltedCalled.get() );
        assertFalse( endedCalled.get() );

    }
}
