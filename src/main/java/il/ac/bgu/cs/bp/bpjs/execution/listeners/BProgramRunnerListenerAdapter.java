/*
 * The MIT License
 *
 * Copyright 2017 BGU.
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
package il.ac.bgu.cs.bp.bpjs.execution.listeners;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;

/**
 * A {@link BProgramRunnerListener} with all methods defaultly implemented. Use when
 * you only need to override some methods.
 * 
 * @author michael
 */
public abstract class BProgramRunnerListenerAdapter implements BProgramRunnerListener {

    @Override
    public void starting(BProgram bprog) {}

    @Override
    public void started(BProgram bp) {}

    @Override
    public void superstepDone(BProgram bp) {}

    @Override
    public void ended(BProgram bp) {}

    @Override
    public void assertionFailed(BProgram bp, FailedAssertion theFailedAssertion) {}

    @Override
    public void bthreadAdded(BProgram bp, BThreadSyncSnapshot theBThread) {}

    @Override
    public void bthreadRemoved(BProgram bp, BThreadSyncSnapshot theBThread) {}

    @Override
    public void bthreadDone(BProgram bp, BThreadSyncSnapshot theBThread) {}

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {}

    @Override
    public void halted(BProgram bp) {}
    
}
