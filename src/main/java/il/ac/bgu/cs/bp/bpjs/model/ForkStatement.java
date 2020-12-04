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

import il.ac.bgu.cs.bp.bpjs.bprogramio.BThreadSyncSnapshotInputStream;
import il.ac.bgu.cs.bp.bpjs.bprogramio.BThreadSyncSnapshotOutputStream;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StreamObjectStub;
import il.ac.bgu.cs.bp.bpjs.bprogramio.StubProvider;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BProgramJsProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy.Modification;
import il.ac.bgu.cs.bp.bpjs.execution.tasks.BPEngineTask;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data required for performing {@code bp.fork}.
 * 
 * TODO: Add array of parameters such that each for gets it's own value (parent should get {@code undefined}).
 * 
 * @author michael
 */
public class ForkStatement {
    
    private Object bthreadData;
    private Map<String, Modification<Object>> storageModifications;
    private Object continuation;
    
    private BThreadSyncSnapshot forkingBThread;

    public ForkStatement(Object aContinuation) {
        continuation = aContinuation;        
    }
    
    /**
     * Clones the data of the forked b-thread, so that the original thread can 
     * continue without changing the fork's data.
     * @param syncOrigin The b-program sync snapshot out of which the forking b-thread started
     */
    public void cloneBThreadData(BProgramSyncSnapshot syncOrigin){
        byte[] serializedForm;
        
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BThreadSyncSnapshotOutputStream btos = new BThreadSyncSnapshotOutputStream(baos, syncOrigin.getBProgram().getGlobalScope()) ) {
            btos.writeObject(bthreadData);
            btos.writeObject(storageModifications);
            btos.writeObject(continuation);
            btos.flush();
            baos.flush();
            serializedForm = baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(BPEngineTask.class.getName()).log(Level.SEVERE, "Error while serializing continuation during fork:" + ex.getMessage(), ex);
            throw new RuntimeException("Error while serializing continuation during fork:" + ex.getMessage(), ex);
        }
        
        final BProgramJsProxy bpProxy = new BProgramJsProxy(syncOrigin.getBProgram());

        StubProvider stubPrv = (StreamObjectStub stub) -> {
            if (stub == StreamObjectStub.BP_PROXY) {
                return bpProxy;
            }
            throw new IllegalArgumentException("Unknown stub " + stub);
        };
        
        try ( ByteArrayInputStream bais = new ByteArrayInputStream(serializedForm);
              BThreadSyncSnapshotInputStream btis = new BThreadSyncSnapshotInputStream(bais, syncOrigin.getBProgram().getGlobalScope(), stubPrv)
            ) {
            
            bthreadData = btis.readObject();
            storageModifications = (Map<String, Modification<Object>>) btis.readObject();
            continuation = btis.readObject();
            
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(BPEngineTask.class.getName()).log(Level.SEVERE, "Error while serializing continuation during fork:" + ex.getMessage(), ex);
            throw new RuntimeException("Error while serializing continuation during fork:" + ex.getMessage(), ex);
        }
    }
    
    public BThreadSyncSnapshot makeSnapshot(String aName, BProgramSyncSnapshot syncOrigin) {
        return new BThreadSyncSnapshot(
            aName,
            forkingBThread.getEntryPoint(),
            forkingBThread.getInterrupt().orElse(null),
            continuation,
            null,
            bthreadData,
            new MapProxy(syncOrigin.getDataStore(), storageModifications)
        );
    }
    
    public Object getContinuation() {
        return continuation;
    }

    public void setForkingBThread(BThreadSyncSnapshot forkingBThread) {
        this.forkingBThread = forkingBThread;
        setBthreadData(forkingBThread.getData());
        storageModifications = forkingBThread.getStorageModifications();
    }

    public BThreadSyncSnapshot getForkingBThread() {
        return forkingBThread;
    }

    public Object getBthreadData() {
        return bthreadData;
    }

    public void setBthreadData(Object bthreadData) {
        this.bthreadData = bthreadData;
    }
    
}
