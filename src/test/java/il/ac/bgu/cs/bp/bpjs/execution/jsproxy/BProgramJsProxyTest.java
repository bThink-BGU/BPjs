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
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.analysis.BThreadSnapshotVisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import org.junit.Test;

import static il.ac.bgu.cs.bp.bpjs.TestUtils.traceEventNamesString;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.DeadlockViolation;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author michael
 */
public class BProgramJsProxyTest {
    
    @Test
    public void randomProxyTest() throws InterruptedException {
        BProgram sut = new ResourceBProgram("RandomProxy.js");
        
        new BProgramRunner(sut).run();
        Double boolCount = sut.getFromGlobalScope("boolCount", Double.class).get();
        assertEquals(500.0, boolCount, 100);
        
        Double intCount = sut.getFromGlobalScope("intCount", Double.class).get();
        assertEquals(500.0, intCount, 100);
        
        Double floatCount = sut.getFromGlobalScope("floatCount", Double.class).get();
        assertEquals(500.0, floatCount, 100);
    }

    @Test
    public void logLevelProxyTest() throws InterruptedException {
        
        BProgram sut = new ResourceBProgram("RandomProxy.js");
        
        new BProgramRunner(sut).run();
        String logLevel1 = sut.getFromGlobalScope("logLevel1", String.class).get();
        assertEquals(BpLog.LogLevel.Off.name(), logLevel1);
        
        String logLevel2 = sut.getFromGlobalScope("logLevel2", String.class).get();
        assertEquals(BpLog.LogLevel.Warn.name(), logLevel2);
    }
    
    @Test
    public void testEquality() {
        BProgram bprog = new ResourceBProgram("RandomProxy.js");
        new BProgramRunner(bprog).run();
        BProgramJsProxy sut1 = bprog.getFromGlobalScope("bp", BProgramJsProxy.class).get();
        assertEquals( sut1, sut1 );
        assertNotEquals(sut1, null);
        assertNotEquals(sut1, "String");
        
        bprog = new ResourceBProgram("RandomProxy.js");
        new BProgramRunner(bprog).run();
        BProgramJsProxy sut2 = bprog.getFromGlobalScope("bp", BProgramJsProxy.class).get();
        assertNotEquals( sut1, sut2 );
        
        Map<String, BProgramJsProxy> map = new HashMap<>();
        map.put("sut1", sut1);
        map.put("sut2", sut2);
        
        assertSame( sut1, map.get("sut1") );
        assertSame( sut2, map.get("sut2") );
    }
    
    @Test
    public void DeadlockSameThread() throws Exception{
        BProgram bpr = new ResourceBProgram("bpsync-blockrequest.js");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        sut.setVisitedStateStore(new BThreadSnapshotVisitedStateStore());
        VerificationResult res = sut.verify(bpr);
        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DeadlockViolation);
        assertEquals("sampleEvent", traceEventNamesString(res.getViolation().get().getCounterExampleTrace(), ""));
        
        BProgram bprPred = new ResourceBProgram("bpsync-blockrequestPredicate.js");
        res = sut.verify(bprPred);
        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof DeadlockViolation);
        assertEquals("sampleEvent", traceEventNamesString(res.getViolation().get().getCounterExampleTrace(), ""));
    }
}
