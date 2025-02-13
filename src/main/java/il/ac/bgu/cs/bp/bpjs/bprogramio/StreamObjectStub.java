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
package il.ac.bgu.cs.bp.bpjs.bprogramio;

import java.util.Objects;

/**
 * Placeholders for non-serializable objects in the  bthread serialization stream.
 * Used for breaking connections from the JavaScript serialized context to the Java context.
 * 
 * @author michael
 */
public class StreamObjectStub implements java.io.Serializable {
    
    public static final StreamObjectStub BP_PROXY = new StreamObjectStub("bp-proxy", null);
    
    private final String stubberId;
    private final Object data;

    public StreamObjectStub(String stubberId, Object data) {
        this.stubberId = stubberId;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public String getStubberId() {
        return stubberId;
    }
    
    @Override
    public String toString() {
        return "[Stub stubber:"+stubberId+" data: " + data + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( obj == null ) return false;
        if ( obj == this ) return true;
        if ( ! (obj instanceof StreamObjectStub) ) return false;
        final StreamObjectStub other = (StreamObjectStub)obj;
        
        return Objects.equals(other.stubberId, stubberId) && 
                Objects.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stubberId, data);
    }
}

