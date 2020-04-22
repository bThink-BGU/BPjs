package il.ac.bgu.cs.bp.bpjs.mocks;


import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.internal.ContinuationProgramState;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * The MIT License
 *
 * Copyright 2019 michael.
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

/**
 *
 * @author michael
 */
public class MockBThreadSyncSnapshot extends BThreadSyncSnapshot {
    
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
    
    public static class MockContinuationProgramState extends ContinuationProgramState {

        public void setFrameIndex(int frameIndex) {
            this.frameIndex = frameIndex;
        }

        public void setProgramCounter(int programCounter) {
            this.programCounter = programCounter;
        }
        
        public void setVariable( String name, Object value ) {
            variables.put(name, value);
        }
    }
    
    public MockBThreadSyncSnapshot(String aName, SyncStatement stmt ) {
        super(aName, null);
        setSyncStatement(stmt);
        stmt.setBthread(this);
    }
    
    public MockBThreadSyncSnapshot(SyncStatement stmt ) {
        this( "mock-btss_" + NEXT_ID.incrementAndGet(), stmt );
    }
    
}
