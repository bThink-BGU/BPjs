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
package il.ac.bgu.cs.bp.bpjs.internal;

import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Conflict;
import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult.Success;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.stream.Collectors.toList;

/**
 * Takes multiple map proxies, consolidates to a new map, or to a detailed
 * failure report.
 * 
 * @author michael
 */
public class MapProxyConsolidator {
      
    public StorageConsolidationResult consolidate( Collection<BThreadSyncSnapshot> bts ) {
        
        Map<String, Map<BThreadSyncSnapshot, MapProxy.Modification<Object>>> consolidatedView = new HashMap<>();
        
        // Consolidate the view
        bts.forEach( bt -> {
            bt.getBprogramStoreModifications().forEach((key,mod)->{
                Map<BThreadSyncSnapshot, MapProxy.Modification<Object>> mods = consolidatedView.get(key);
                if ( mods == null ) {
                    mods = new HashMap<>();
                    consolidatedView.put(key, mods);
                }
                mods.put(bt, mod);
            });
        });
        
        // find conflicts (if any)
        var conflicts = consolidatedView.entrySet().stream()
            .filter( e -> containsConflict(e) )
            .collect( toList() );
        
        if ( conflicts.isEmpty() ) {
            // generate changeset
            Map<String, MapProxy.Modification<Object>> changes = new HashMap<>();
            consolidatedView.entrySet().forEach( e->{
               changes.put( e.getKey(), e.getValue().values().iterator().next() );
            });
                
            return new Success(changes);
            
        } else {
            
            Map<String, Map<BThreadSyncSnapshot, MapProxy.Modification<Object>>> cnfs = new HashMap<>();
            conflicts.forEach( e -> {
                cnfs.put(e.getKey(), e.getValue());
            });
            return new Conflict(cnfs);
            
        }
        
    }
    
    
    private boolean containsConflict( Map.Entry<String, Map<BThreadSyncSnapshot, MapProxy.Modification<Object>>> ent ) {
        
        var mods = ent.getValue();
        
        // must have at least 2 instructions for conflict
        if ( mods.size() < 2 ) return false;
        
        // We have a conflict if at least 2 modifications don't agree 
        return new HashSet<>(mods.values()).size() > 1;
    }
    
    
}
