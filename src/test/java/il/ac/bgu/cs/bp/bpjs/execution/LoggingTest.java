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

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BpListLog;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BpLog;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class LoggingTest {
    
    @Test
    public void testLogLevels() throws InterruptedException, UnsupportedEncodingException, IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream myOut = new PrintStream(baos)) {
            final ResourceBProgram bprog = new ResourceBProgram("logging/simple.js");
            bprog.setLoggerOutputStreamer(myOut);
            new BProgramRunner( bprog).run();
            myOut.flush();
        }
        String result = baos.toString(StandardCharsets.UTF_8.name());
        
        System.out.println("result:");
        System.out.println(result);
        
        org.junit.Assert.assertEquals(10l, (long)result.split("\n").length);
        baos.close();
    }

    @Test
    public void testNewLogImpLevels() throws InterruptedException, UnsupportedEncodingException, IOException {


        final ResourceBProgram bprog = new ResourceBProgram("logging/simple.js");

        var Log = new BpListLog();
        bprog.setLogger(Log);
        new BProgramRunner( bprog).run();

        System.out.println("result:");
        System.out.println("info" + Log.getInfo().toString());
        System.out.println("warn" + Log.getWarn().toString());
        System.out.println("fine" + Log.getFine().toString());
        System.out.println("off" + Log.getOff().toString());

        org.junit.Assert.assertEquals(5l, (long)Log.getInfo().size());
        org.junit.Assert.assertEquals(5l, (long)Log.getWarn().size());
        org.junit.Assert.assertEquals(5l, (long)Log.getFine().size());
        org.junit.Assert.assertEquals(5l, (long)Log.getError().size());
        org.junit.Assert.assertEquals(0l, (long)Log.getOff().size());
    }

    @Test
    public void testFormatting() throws InterruptedException, UnsupportedEncodingException, IOException {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream myOut = new PrintStream(baos)) {
            System.setOut(myOut);
            new BProgramRunner( new StringBProgram(
                "bp.log.info(\"{0}\",1000);\n" +
                "bp.log.info(\"{0}\",{a:1,b:2, c:\"banana\", arr:[1,2,3]});\n" +
                "bp.log.info(\"0\");\n" +
                "bp.log.info(null);\n" +
                "")).run();
            myOut.flush();
        }
        String result = baos.toString(StandardCharsets.UTF_8.name());
        System.setOut(originalOut);
        baos.close();
        
        System.out.println("result:");
        System.out.println(result);
        
        String[] lines = result.split("\n");
        assertTrue(lines[0].contains("1,000") );
        assertTrue(lines[1].contains("a:1") );
        assertTrue(lines[1].contains("b:2") );
        assertTrue(lines[2].contains("0") );
        assertTrue(lines[3].contains("null") );
    }
    
    
    @Test
    public void testExternalSetLogLevel() throws UnsupportedEncodingException, IOException {
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
            bprog.setLoggerOutputStreamer(myOut);
            new BProgramRunner(bprog).run();
            myOut.flush();
        }
        String result1 = baos.toString(StandardCharsets.UTF_8);
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
        String result2 = baos.toString(StandardCharsets.UTF_8);
        org.junit.Assert.assertEquals(1l, (long)result2.split("\n").length);
        
        System.setOut(originalOut);
        System.out.println("Result 1:");
        System.out.println(result1);
        System.out.println();
        System.out.println("Result 2:");
        System.out.println(result2);
        baos.close();
    }
    
    @Test
    public void testCompoundObjectLogging() throws IOException {
        PrintStream originalOut = System.out;
        String result;
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PrintStream myOut = new PrintStream(baos)) {
                System.setOut(myOut);
                
                new BProgramRunner(new ResourceBProgram("logging/compound.js")).run();
                myOut.flush();
                
            } finally {
                System.setOut(originalOut);
            }
            result = baos.toString(StandardCharsets.UTF_8);    
        }
        
        System.out.println(result);
        
        assertTrue(result.contains("Set"));
        assertTrue(result.contains("List"));
        assertTrue(result.contains("Map"));
        assertTrue(result.contains("->"));
        assertTrue(result.contains("|"));
        assertTrue(result.contains("{"));
        assertTrue(result.contains("}"));
    }
    
    @Test
    public void testFormattedObjectLogging() throws IOException {
        String result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                      PrintStream myOut = new PrintStream(baos))
        {
            final ResourceBProgram bprog = new ResourceBProgram("logging/withFormat.js");
            bprog.setLoggerOutputStreamer(myOut);
            new BProgramRunner(bprog).run();

            myOut.flush();
            result = baos.toString(StandardCharsets.UTF_8);   
            
        }
        
        System.out.println(result);
        final String[] lines = result.split("\n", -1);
        long linesWithJsArray = Arrays.asList(lines).stream().filter(s->s.contains("[")).count();
        assertEquals(2, linesWithJsArray);
    }
    
}
