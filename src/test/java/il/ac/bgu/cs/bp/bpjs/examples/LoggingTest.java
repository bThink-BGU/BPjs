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
package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BpLog;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class LoggingTest {
    
    @Test
    public void testLogLevels() throws InterruptedException, UnsupportedEncodingException {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream myOut = new PrintStream(baos)) {
            System.setOut(myOut);
            new BProgramRunner( new SingleResourceBProgram("loggingTest.js")).run();
            myOut.flush();
        }
        String result = baos.toString(StandardCharsets.UTF_8.name());
        System.setOut(originalOut);
        
        System.out.println("result:");
        System.out.println(result);
        
        org.junit.Assert.assertEquals(6l, (long)result.split("\n").length);
    }
    
    @Test
    public void testExternalSetLogLevel() throws UnsupportedEncodingException {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream myOut = new PrintStream(baos)) {
            System.setOut(myOut);
            BProgram bprog = new StringBProgram(
                "bp.registerBThread(function(){\n" +
                "  bp.sync({request:bp.Event(\"x\")});\n" +
                "  bp.log.fine(\"msg\");\n" +
                "  bp.log.info(\"msg\");\n" +
                "  bp.log.warn(\"msg\");\n" +
                "});");
            bprog.setLogLevel(BpLog.LogLevel.Fine);
            new BProgramRunner(bprog).run();
            myOut.flush();
        }
        String result1 = baos.toString(StandardCharsets.UTF_8.name());
        org.junit.Assert.assertEquals(3l, (long)result1.split("\n").length);
        
        baos = new ByteArrayOutputStream();
        try (PrintStream myOut = new PrintStream(baos)) {
            System.setOut(myOut);
            BProgram bprog = new StringBProgram(
                "bp.registerBThread(function(){\n" +
                "  bp.sync({request:bp.Event(\"x\")});\n" +
                "  bp.log.fine(\"msg\");\n" +
                "  bp.log.info(\"msg\");\n" +
                "  bp.log.warn(\"msg\");\n" +
                "});");
            bprog.setLogLevel(BpLog.LogLevel.Warn);
            new BProgramRunner(bprog).run();
            myOut.flush();
        }
        String result2 = baos.toString(StandardCharsets.UTF_8.name());
        org.junit.Assert.assertEquals(1l, (long)result2.split("\n").length);
        
        System.setOut(originalOut);
        System.out.println("Result 1:");
        System.out.println(result1);
        System.out.println();
        System.out.println("Result 2:");
        System.out.println(result2);
    }
}
