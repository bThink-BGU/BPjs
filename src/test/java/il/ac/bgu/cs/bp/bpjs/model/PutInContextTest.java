/*
 * The MIT License
 *
 * Copyright 2017 BGU.
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
package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test that we can put Objects in the global context.
 * @author michael
 */
public class PutInContextTest {
    
    @Test
    public void testPutInContext_global() throws InterruptedException {
        BProgram sut = new StringBProgram("bp.log.info(obj);var len=obj.length;");
        String outsideObject = "I'm an outside object";
        sut.putInGlobalScope("obj", outsideObject);
        new BProgramRunner(sut).run();
        assertEquals( sut.getFromGlobalScope("len", Long.class).get().longValue(), outsideObject.length() );
    }
    
    @Test
    public void testPutInContext_midRun() throws InterruptedException {
        final String program = "bp.registerBThread(function(){\n"
                + "bsync({request:bp.Event('A')});\n"
                + "bp.log.info(obj);var len=obj.length;\n"
                + "bsync({request:bp.Event(String(len))});\n"
                + "});";
        final String outsideObject = "I'm an outside object";
        
        BProgram sut = new StringBProgram(program);
        BProgramRunner runner = new BProgramRunner(sut);
        runner.addListener(new BProgramRunnerListenerAdapter() {
            int counter = 0;
            @Override
            public void eventSelected(BProgram bp, BEvent theEvent) {
                switch (counter) {
                    case 0:
                        sut.putInGlobalScope("obj", outsideObject);
                        break;
                    case 1:
                        assertEquals( theEvent.getName(), Long.toString(outsideObject.length()) );
                        break;
                }
                counter++;
            }
        } );
        runner.run();
    }
}
