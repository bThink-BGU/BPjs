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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import java.util.List;
import java.util.Set;

/**
 * Result of a program verification.
 * 
 * @author michael
 */
public class VerificationResult {

    public BEvent getInvalidEvent() {
        return invalidEvent;
    }

    /**
     * The reason a b-program failed verification.
     */
    public enum ViolationType {
        /** No violation was found (program was successfully verified) */
        None,
        
        /** Program contains deadlocks while is shouldn't */
        Deadlock,

        /** Program can reach a state we don't want to reach */
        BadState,
        
        /** Program can generate an illegal event trace */
        FailedAssertion
    }
    
    private final long timeMillies;
    private final long statesScanned;
    private final List<Node> counterExampleTrace;
    private final ViolationType violationType;
    private final FailedAssertion failedAssertion;
    private final BEvent invalidEvent;

    public VerificationResult( ViolationType aViolationType, FailedAssertion aFailedAssertion, BEvent aInvalidEvent, List<Node> counterExampleTrace, long timeMillies, long statesScanned ) {
        failedAssertion = aFailedAssertion;
        violationType = aViolationType;
        this.timeMillies = timeMillies;
        this.statesScanned = statesScanned;
        this.counterExampleTrace = counterExampleTrace;
        this.invalidEvent = aInvalidEvent;
    }
    
    VerificationResult( ViolationType vt, FailedAssertion fa, List<Node> trace ) {
        this(vt, fa, null, trace, 0,0);
    }

    VerificationResult( ViolationType vt, BEvent ie, List<Node> trace ) {
        this(vt, null, ie, trace, 0,0);
    }
    VerificationResult( ViolationType vt, List<Node> trace ) {
        this(vt, null, null, trace, 0,0);
    }
    
    public long getTimeMillies() {
        return timeMillies;
    }

    public long getScannedStatesCount() {
        return statesScanned;
    }

    public List<Node> getCounterExampleTrace() {
        return counterExampleTrace;
    }
    
    public boolean isCounterExampleFound() {
        return counterExampleTrace!=null;
    }

    public ViolationType getViolationType() {
        return violationType;
    }
    
    public boolean isVerifiedSuccessfully() {
        return violationType == ViolationType.None;
    }

    public FailedAssertion getFailedAssertion() {
        return failedAssertion;
    }
    
}
