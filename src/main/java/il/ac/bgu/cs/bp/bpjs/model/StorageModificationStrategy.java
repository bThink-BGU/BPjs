/*
 * The MIT License
 *
 * Copyright 2020 michael.
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

import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Conflict;
import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Success;
import java.util.Set;

/**
 * A strategy that handles modifications to a b-program's data store.
 * @author michael
 */
public interface StorageModificationStrategy {
    
    /**
     * A set of modifications are requested to be applied to {@code bpss}'s storage.
     * These are the modification requests coming from the b-threads as part of
     * their synchronization process. 
     * This method may modify the requested changes as needed.
     * 
     * @param modifications The modifications requested to be applied.
     * @param bpss the b-program sync snapshot the sync cycle started from.
     * @param nextRoundBThreads The set of b-threads that will take part in the b-program's future cycles (including blocked ones). Modifiable.
     * @return A set of (possibly modified) modifications to be applied to the store.
     */
    StorageConsolidationResult.Success incomingModifications(
        Success modifications,
        BProgramSyncSnapshot bpss, 
        Set<BThreadSyncSnapshot> nextRoundBThreads
    );
        
    /**
     * A chance to resolve a modification conflict. This conflict since some b-thread modification requests
     * do not agree with each other.
     * 
     * @param conflict The conflict description
     * @param bpss the b-program sync snapshot the sync cycle started from.
     * @param nextRoundBThreads The set of b-threads that will take part in the b-program's future cycles (including blocked ones). Modifiable.
     * @return A resolved conflict (using {@link il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Success}), or a {@code Conflict}.
     * @see il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Success
     */
    StorageConsolidationResult resolve(
        Conflict conflict, 
        BProgramSyncSnapshot bpss,
        Set<BThreadSyncSnapshot> nextRoundBThreads
    );
    
    /**
    * A default implementation of {@code StorageModificationStrategy}. Does not modify values 
    * passed to it.
    * 
    * @author michael
    */
    public final StorageModificationStrategy PASSTHROUGH = new StorageModificationStrategy(){

        @Override
        public Success incomingModifications(Success modifications, BProgramSyncSnapshot bpss, Set<BThreadSyncSnapshot> nextRoundBThreads) {
            return modifications;
        }

        @Override
        public StorageConsolidationResult resolve(Conflict conflict, BProgramSyncSnapshot bpss, Set<BThreadSyncSnapshot> nextRoundBThreads) {
            return conflict;
        }
    };

}
