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
package il.ac.bgu.cs.bp.bpjs.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A factory class for making executor services. Threads in those executor
 * can have semantic names.
 * 
 * @author michael
 */
public abstract class ExecutorServiceMaker {
    
    public static ExecutorService makeWithName( String threadNameTemplate ) {
        final ThreadFactory dtf = Executors.defaultThreadFactory();
        final AtomicInteger threadCoutner = new AtomicInteger(0);
        ThreadFactory tf = (Runnable r) -> {
            Thread retVal = dtf.newThread(r);
            retVal.setName(threadNameTemplate + "#" + threadCoutner.incrementAndGet() );
            return retVal;
        };
        return Executors.newCachedThreadPool(tf);
    }
    
    private ExecutorServiceMaker(){
        // prevent instantiation
    }
    
}
