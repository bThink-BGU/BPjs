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
package il.ac.bgu.cs.bp.bpjs.model;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.InMemoryEventLoggingListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class ResourceBProgramTest {
    
    @Test
    public void testMissingResource() {
        final String missingResourceName = "I'm not there";
        try {
            new ResourceBProgram(missingResourceName);
        } catch ( IllegalArgumentException iae ) {
            assertTrue(iae.getMessage().contains(missingResourceName));
        }
    }
    
    @Test
    public void testMultipleResources_variableArity() {
        ResourceBProgram sut = new ResourceBProgram("EventArrays.js", "HotNCold.js");
        BProgramRunner rnr = new BProgramRunner(sut);
        
        InMemoryEventLoggingListener eventLogger = rnr.addListener( new InMemoryEventLoggingListener() );
        rnr.run();
        
        Set<String> eventNames = new TreeSet<>(eventLogger.eventNames());
        
        assertEquals( new TreeSet<>(Arrays.asList(
                          "e11","e21", "coldEvent","hotEvent","allDone"
                        )),
                        eventNames);
    }
    
    @Test
    public void testMultipleResources_Collection() {
        List<String> resNames = new ArrayList<>();
        resNames.add("EventArrays.js");
        resNames.add("HotNCold.js");
        
        ResourceBProgram sut = new ResourceBProgram(resNames);
        BProgramRunner rnr = new BProgramRunner(sut);
        
        assertEquals("EventArrays.js+HotNCold.js", sut.getName());
        
        InMemoryEventLoggingListener eventLogger = rnr.addListener( new InMemoryEventLoggingListener() );
        rnr.run();
        
        Set<String> eventNames = new TreeSet<>(eventLogger.eventNames());
        
        assertEquals( new TreeSet<>(Arrays.asList(
                          "e11","e21", "coldEvent","hotEvent","allDone"
                        )),
                        eventNames);
    }
    
}
