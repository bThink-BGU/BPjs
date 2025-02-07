/*
 * The MIT License
 *
 * Copyright 2025 michael.
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

import java.util.Set;

/**
 * An object that allows serialization of non-serializable objects. This is done
 * by providing serializable stub for the serialization stream, and then converting
 * stubs to data and vice versa.
 * 
 * @author michael
 */
public interface SerializationStubber {
   
    /**
     * Gets the id of the stubber, needed to de-stub the stubs.
     * @return id of this stubber.
     */
    String getId();
   
    /**
     * 
     * @return Set of classes this stubber can stub.
     */
    Set<Class> getClasses();
    
    /**
     * Generate a stub for the given object.
     * @param in a (probably non-serializable) object to be replaced by a stub.
     * @return a serializable stub for the object.
     */
    StreamObjectStub stubFor( Object in );
    
    /**
     * Parse a stub into an original object.
     * @param aStub The stub to parse.
     * @return An object.
     */
    Object objectFor( StreamObjectStub aStub );
}
