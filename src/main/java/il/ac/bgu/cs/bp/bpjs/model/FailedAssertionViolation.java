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
package il.ac.bgu.cs.bp.bpjs.model;

import java.util.Objects;

/**
 * Captures a failed assertion done by a b-thread. This is normally used to capture
 * cases where some specification was violated.
 * 
 * @author michael
 */
public class FailedAssertionViolation extends SafetyViolation implements java.io.Serializable {
    private final String bThreadName;

    public FailedAssertionViolation(String message, String bThreadName) {
        super(message);
        this.bThreadName = bThreadName;
    }


    public String getBThreadName() {
        return bThreadName;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.bThreadName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FailedAssertionViolation)) {
            return false;
        }
        final FailedAssertionViolation other = (FailedAssertionViolation) obj;
        return Objects.equals(this.bThreadName, other.bThreadName) && super.equals(obj);
    }

    @Override
    public String toString() {
        return "[FailedAssertion message:" + message + ", b-thread:" + bThreadName + ']';
    }
    
    
}
