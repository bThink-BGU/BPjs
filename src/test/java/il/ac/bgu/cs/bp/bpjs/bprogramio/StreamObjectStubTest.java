/*
 * The MIT License
 *
 * Copyright 2021 michael.
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

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class StreamObjectStubTest {
    /**
     * Test of toString method, of class StreamObjectStub.
     */
    @Test
    public void testBasics() {
        StreamObjectStub instance = new StreamObjectStub("Test");
        String result = instance.toString();
        assertTrue( result.contains("Test") );
        Set<StreamObjectStub> stubSet = new HashSet<>();
        stubSet.add( new StreamObjectStub("A") );
        stubSet.add( new StreamObjectStub("B") );
        stubSet.add( new StreamObjectStub("C") );
        stubSet.add( new StreamObjectStub("B") );
        assertEquals( 3, stubSet.size() );
    }

    /**
     * Test of equals method, of class StreamObjectStub.
     */
    @Test
    public void testEquals() {
        Object sut = new StreamObjectStub("sut");
        assertFalse( sut.equals(null) );
        assertFalse( sut.equals("String") );
        assertFalse( sut.equals(new StreamObjectStub("not-sut")) );
        assertTrue( sut.equals(new StreamObjectStub("sut")) );
        assertTrue( sut.equals(sut) );
    }
    
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        
        StreamObjectStub sutA = new StreamObjectStub("A");
        StreamObjectStub sutB = new StreamObjectStub("B");
        StreamObjectStub sutC = new StreamObjectStub("C");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(StreamObjectStub.BP_PROXY);
        oos.writeObject( sutA );
        oos.writeObject( sutB );
        oos.writeObject( sutC );
        
        oos.flush();
        oos.close();
        baos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        
        assertSame( StreamObjectStub.BP_PROXY, ois.readObject() );
        Object outA = ois.readObject();
        
        assertEquals( sutA, outA );
        assertNotSame( sutA, outA );
    }
    
    @Test
    public void testCustomSerializations() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("custom-serializations.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier(bprog);
        
        // We just check that there are no serialization / de-serialization errors.
        vfr.verify(bprog);
    }
}
