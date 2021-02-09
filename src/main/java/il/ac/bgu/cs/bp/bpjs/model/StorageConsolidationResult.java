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

import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.MapProxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static java.util.stream.Collectors.joining;

/**
 * The result of consolidating storage update requests from multiple b-threads.
 * 
 * @author michael
 */
public abstract class StorageConsolidationResult {
    
       public static class Success extends StorageConsolidationResult {
        
        final Map<String,MapProxy.Modification<Object>> updates;

        public Success(Map<String, MapProxy.Modification<Object>> someUpdates) {
            updates = someUpdates;
        }

        /**
         * Take the passed map, and create a new map that's based on it, but with
         * the modifications applied.
         * 
         * @param in The map we want to modify
         * @return a new map, same as the passed map, but with modifications.
         */
        public Map<String,Object> apply( Map<String, Object> in ) { 
            final Map<String, Object> newMap = new HashMap<>();
            // add new/updated values
            updates.entrySet().stream()
                .filter(  e -> e.getValue() instanceof MapProxy.PutValue)
                .forEach( e -> newMap.put(e.getKey(), ((MapProxy.PutValue<Object>)e.getValue()).getValue()) );
            
            // add non-modified values
            in.entrySet().stream()
                .filter( e -> !updates.keySet().contains(e.getKey()) )
                .forEach( e -> newMap.put(e.getKey(), e.getValue()) );
            
            return newMap;
        }
        
        public Map<String, MapProxy.Modification<Object>> getUpdates() {
            return updates;
        }
    }
       
    public static class Conflict extends StorageConsolidationResult {
        
        // key -> Map(b-t, modification)
        public final Map<String, Map<BThreadSyncSnapshot, MapProxy.Modification<Object>>> conflicts;

        public Conflict(Map<String, Map<BThreadSyncSnapshot, MapProxy.Modification<Object>>> conflicts) {
            this.conflicts = conflicts;
        }
        
        @Override
        public String toString() {
            return "[Conflict " + conflicts.keySet().stream().collect( joining(", ", "{","}")) + "]";
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 13 * hash + Objects.hashCode(this.conflicts);
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
            final Conflict other = (Conflict) obj;
            
            return Objects.equals(this.conflicts, other.conflicts);
        }
        
        
    }
    
}
