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
package il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.jsproxy;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import org.junit.Test;
import org.mozilla.javascript.Context;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author michael
 */
public class BProgramJsProxyTest {
    
    @Test
    public void randomProxyText() throws InterruptedException {
        
        BProgram sut = new SingleResourceBProgram("RandomProxy.js");
        
        sut.start();
        Double boolCount = (Double)Context.jsToJava(sut.getGlobalScope().get("boolCount", sut.getGlobalScope()), Double.class);
        assertEquals(500.0, boolCount, 100);
        
        Double intCount = (Double)Context.jsToJava(sut.getGlobalScope().get("intCount", sut.getGlobalScope()), Double.class);
        assertEquals(500.0, intCount, 100);
        
        Double floatCount = (Double)Context.jsToJava(sut.getGlobalScope().get("floatCount", sut.getGlobalScope()), Double.class);
        assertEquals(500.0, floatCount, 100);

    }
}
