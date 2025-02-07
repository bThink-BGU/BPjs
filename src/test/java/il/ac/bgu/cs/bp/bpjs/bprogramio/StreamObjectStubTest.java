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

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
        StreamObjectStub instance = new StreamObjectStub("testStub", "SOMEDATA");
        String result = instance.toString();
        assertTrue( result.contains("testStub") );
        assertEquals( instance, new StreamObjectStub("testStub", "SOMEDATA") );
        assertNotEquals(instance, new StreamObjectStub("testStub", "XX SOMEDATAX"));
        assertNotEquals(instance, new StreamObjectStub("XX testStub", "SOMEDATA"));
        
        var set = new HashSet<>();
        set.add( new StreamObjectStub("testStub", "SOMEDATA") );
        set.add( new StreamObjectStub("testStub", "SOME xx DATA") );
        set.add( new StreamObjectStub("test xx Stub", "SOMEDATA") );
        set.add( new StreamObjectStub("testStub", "SOMEDATA") );
        
        assertEquals( 3, set.size() );
    }

    
    @Test
    public void testCustomSerialization() throws IOException, ClassNotFoundException {
        
        // Create stubber factory.
        SerializationStubberFactory testFact = (BProgram aBProgram) -> new SerializationStubber(){
            @Override
            public String getId() {
                return "test-stubber";
            }
            
            @Override
            public Set<Class> getClasses() {
                return Set.of(NonSerializable.class);
            }
            
            @Override
            public StreamObjectStub stubFor(Object in) {
                return new StreamObjectStub(getId(), ((NonSerializable)in).getData());
            }
            
            @Override
            public Object objectFor(StreamObjectStub aStub) {
                return new NonSerializable((String) aStub.getData());
            }
        };
        
        // register factory
        BPjs.registerStubberFactory(testFact);
        
        // now, test
        List<NonSerializable> testDataIn = List.of(new NonSerializable("a"), new NonSerializable("ab"), new NonSerializable("abc"));
        List<NonSerializable> testDataOut;
        
        byte[] serializedForm;
        BProgramSyncSnapshotIO dupper = new BProgramSyncSnapshotIO(new StringBProgram("let x = 80;"));
        
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BPJSStubOutputStream btos = dupper.newOutputStream(baos)
        ) { 
            btos.writeObject(testDataIn);
            btos.flush();
            baos.flush();
            serializedForm = baos.toByteArray();
        }
        
        try ( ByteArrayInputStream bais = new ByteArrayInputStream(serializedForm);
              BPJSStubInputStream btis = dupper.newInputStream(bais)
        ) {
            testDataOut = (List<NonSerializable>) btis.readObject();
        }
        
        // clean up global status
        BPjs.unregisterStubberFactory(testFact);
        
        // validate results.
        assertEquals(testDataIn, testDataOut);
    }
    
    @Test
    public void testBuiltInSerializations() throws Exception {
        final ResourceBProgram bprog = new ResourceBProgram("built-in-serializations.js");
        DfsBProgramVerifier vfr = new DfsBProgramVerifier(bprog);
        
        // We just check that there are no serialization / de-serialization errors.
        vfr.verify(bprog);
    }
}

class NonSerializable {
    private final String data;

    public NonSerializable(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.data);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NonSerializable other = (NonSerializable) obj;
        return Objects.equals(this.data, other.data);
    }
    
    
}