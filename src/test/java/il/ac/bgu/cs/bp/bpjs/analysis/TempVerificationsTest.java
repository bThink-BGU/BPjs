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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotTerminationViolation;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import java.util.Collections;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author michael
 */
public class TempVerificationsTest {
    
    
    @Test
    public void testHotTermination() throws Exception{
        final SingleResourceBProgram bprog = new SingleResourceBProgram("statementtemp/hotTerminationExample.js");

        DfsBProgramVerifier vfr = new DfsBProgramVerifier();
        final VerificationResult res = vfr.verify(bprog);

        assertTrue(res.isViolationFound());
        assertTrue(res.getViolation().get() instanceof HotTerminationViolation);
        
        HotTerminationViolation htv = (HotTerminationViolation) res.getViolation().get();
        assertEquals( htv.getBThreadNames(), Collections.singleton("hotter") );
        System.out.println(htv.decsribe());
    }
}
