/*
 * The MIT License
 *
 * Copyright 2017 BPjs Group BGU.
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
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;

import static java.util.Arrays.asList;

import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author michael
 */
public class BProgramTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testGlobalScopeAccessors()  {
        BProgram sut = new SingleResourceBProgram("RandomProxy.js");

        new BProgramRunner(sut).run();

        assertEquals(1000.0, sut.getFromGlobalScope("TOTAL_COUNT", Double.class).get(), 3);
        assertFalse(sut.getFromGlobalScope("does-not-exist", Double.class).isPresent());
    }

    @Test
    public void testDuplicateSetup() {
        String coreSource = "bp.registerBThread(function() {\n" +
                "    bp.sync( {request: bp.Event(\"1\")} );\n" +
                "});";
        BProgram sanitySut = new StringBProgram(coreSource);

        sanitySut.setup();
        exception.expect(IllegalStateException.class);
        sanitySut.setup();

    }


    @Test
    public void testAppendSource() {
        String coreSource = "bp.registerBThread(function() {\n" +
                "    bp.sync( {request: bp.Event(\"1\")} );\n" +
                "    bp.sync( {request: bp.Event(\"2\")} );\n" +
                "    bp.sync( {request: bp.Event(\"3\")} );\n" +
                "});" +
                "bp.log.info('Source code evaluated');";

        String additionalSource = "bp.registerBThread(function(){\n" +
                "   bp.sync({waitFor: bp.Event(\"2\")});\n" +
                "   bp.sync({request: bp.Event(\"2a\"),\n" +
                "            block: bp.Event(\"3\")});\n" +
                "});\n" +
                "bp.log.info('Additional code evaluated');";
        BProgram sanitySut = new StringBProgram(coreSource);

        BProgramRunner rnr = new BProgramRunner(sanitySut);
        rnr.addListener(new PrintBProgramRunnerListener());
        InMemoryEventLoggingListener el = rnr.addListener(new InMemoryEventLoggingListener());
        System.out.println("Sanity run");
        rnr.run();
        System.out.println("/Sanity run");
        System.out.println("");

        assertEquals(asList("1", "2", "3"),
                el.getEvents().stream().map(BEvent::getName).collect(toList()));

        System.out.println("Test Run");
        BProgram sut = new StringBProgram(coreSource);
        sut.appendSource(additionalSource);
        rnr.setBProgram(sut);

        rnr.run();

        assertEquals(asList("1", "2", "2a", "3"),
                el.getEvents().stream().map(BEvent::getName).collect(toList()));

    }

    @Test
    public void testPrependSource() {
        String prependedCode = "var e1=bp.Event(\"1\");\n"
                + "var e2=bp.Event(\"2\");\n"
                + "var e3=bp.Event(\"3\");\n"
                + "bp.log.info('Prepended code evaluated');";

        String coreSource = "bp.registerBThread(function() {\n" +
                "    bp.sync( {request: e1} );\n" +
                "    bp.sync( {request: e2} );\n" +
                "    bp.sync( {request: e3} );\n" +
                "});" +
                "bp.log.info('Source code evaluated');";

        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        InMemoryEventLoggingListener el = rnr.addListener(new InMemoryEventLoggingListener());
        BProgram sut = new StringBProgram(coreSource);
        sut.prependSource(prependedCode);
        rnr.setBProgram(sut);

        rnr.run();

        assertEquals(asList("1", "2", "3"),
                el.getEvents().stream().map(BEvent::getName).collect(toList()));

    }

    @Test
    public void testPrependAndAppendSource() {
        System.out.println("\n\ntestPrependAndAppendSource");
        String prependedCode = "var e1=bp.Event(\"1\");\n"
                + "var e2=bp.Event(\"2\");\n"
                + "var e3=bp.Event(\"3\");\n"
                + "bp.log.info('prepended code evaluated');\n"
                + "var o1=1; var o2=1; var o3=1;";

        String coreSource = "bp.registerBThread(function() {\n" +
                "    bp.sync( {request: e1} );\n" +
                "    bp.sync( {request: e2} );\n" +
                "    bp.sync( {request: e3} );\n" +
                "});" +
                "bp.log.info('main code evaluated');\n"
                + "var o2=2; var o3=2;";

        String appendedSource = "bp.registerBThread(function(){\n" +
                "   bp.sync({waitFor: e2});\n" +
                "   bp.sync({request: bp.Event(\"2a\"),\n" +
                "            block: e3});\n" +
                "});\n" +
                "bp.log.info('appended code evaluated');\n"
                + "var o3=3;";

        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        InMemoryEventLoggingListener el = rnr.addListener(new InMemoryEventLoggingListener());
        BProgram sut = new StringBProgram(coreSource);
        sut.prependSource(prependedCode);
        sut.appendSource(appendedSource);
        rnr.setBProgram(sut);

        rnr.run();

        assertEquals(asList("1", "2", "2a", "3"),
                el.getEvents().stream().map(BEvent::getName).collect(toList()));

        assertEquals(Optional.of(1.0), sut.getFromGlobalScope("o1", Number.class));
        assertEquals(Optional.of(2.0), sut.getFromGlobalScope("o2", Number.class));
        assertEquals(Optional.of(3.0), sut.getFromGlobalScope("o3", Number.class));

        System.out.println("-- testPrependAndAppendSource DONE");
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalAppend() {
        String coreSource = "bp.registerBThread(function() {\n" +
                "    bp.sync( {request: bp.Event(\"1\")} );\n" +
                "    bp.sync( {request: bp.Event(\"2\")} );\n" +
                "    bp.sync( {request: bp.Event(\"3\")} );\n" +
                "});" +
                "bp.log.info('Source code evaluated');";

        BProgram sut = new StringBProgram(coreSource);
        BProgramRunner rnr = new BProgramRunner(sut);
        rnr.run();
        sut.appendSource("bp.log.info('grrr');");
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalPrepend() {
        String coreSource = "bp.registerBThread(function() {\n" +
                "    bp.sync( {request: bp.Event(\"1\")} );\n" +
                "    bp.sync( {request: bp.Event(\"2\")} );\n" +
                "    bp.sync( {request: bp.Event(\"3\")} );\n" +
                "});" +
                "bp.log.info('Source code evaluated');";

        BProgram sut = new StringBProgram(coreSource);
        BProgramRunner rnr = new BProgramRunner(sut);
        rnr.run();
        sut.prependSource("bp.log.info('grrr');");
    }
}
