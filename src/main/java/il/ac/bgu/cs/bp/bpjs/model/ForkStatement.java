/*
 * The MIT License
 *
 * Copyright 2018 BPjs team.
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

/**
 * Data required for performing {@code bp.fork}.
 * 
 * TODO: Add array of parameters such that each for gets it's own value (parent should get {@code undefined}).
 * 
 * @author michael
 */
public class ForkStatement {
    
    private final Object continuation;
    private byte[] serializedContinuation;
    private BThreadSyncSnapshot forkingBThread;

    public ForkStatement(Object aContinuation) {
        continuation = aContinuation;        
    }
    
    public Object getContinuation() {
        return continuation;
    }

    public void setForkingBThread(BThreadSyncSnapshot forkingBThread) {
        this.forkingBThread = forkingBThread;
    }

    public BThreadSyncSnapshot getForkingBThread() {
        return forkingBThread;
    }

    public byte[] getSerializedContinuation() {
        return serializedContinuation;
    }

    public void setSerializedContinuation(byte[] serializedContinuation) {
        this.serializedContinuation = serializedContinuation;
    }
    
}
