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
package il.ac.bgu.cs.bp.bpjs.execution.tasks;

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.bprogramio.log.BpListLog;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class BPEngineTaskTest {

    @Test
    public void testSelfBlockWarning() {

        final BpListLog curLogger = BPjs.setLogger(new BpListLog());

        BProgram bpr = new ResourceBProgram("bpsync-blockrequest.js");
        BProgramRunner rnr = new BProgramRunner(bpr);

        rnr.run();
        String outRes = curLogger.getAll().stream().collect(joining("\n"));
        assertTrue(outRes.contains("helloer")); // name of the self-blocking b-thread
        assertTrue(outRes.contains("Warn")); // There's a warning there too.

    }
}
