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
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import org.mozilla.javascript.NativeObject;

/**
 * A base class for gradually building {@link SyncStatement}s.
 * 
 * Implementation detail: We're using abstract class rather than interface, to 
 * allow for the {@link #synchronizationPoint(org.mozilla.javascript.NativeObject, Boolean, java.lang.Object)}
 * method to be package-private.
 * 
 * @author michael
 */
public abstract class SyncStatementBuilder implements java.io.Serializable {
    
    public void sync( NativeObject jsRWB ) {
        sync(jsRWB, null);
    }
    
    public abstract void sync( NativeObject jsRWB, Object data );
    
    public abstract SyncStatementBuilder hot( boolean isHot );
    
    abstract void synchronizationPoint( NativeObject jsRWB, Boolean hot, Object data );
    
}
