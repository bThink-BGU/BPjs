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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A Proxy to a map.Stores changes to the proxied map, but does not change it.
 * @author michael
 * @param <K> Type of the keys in the map.
 * @param <V> Type of values in the map.
 */
public class MapProxy<K,V> {
    
    public static abstract class Modification<VV> {
        public static final Modification DELETE = new Modification(){};
    }
    
    public static class PutValue<VV> extends Modification<VV> {
        final VV value;

        public PutValue(VV aValue) {
            value = aValue;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)  return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final PutValue other = (PutValue) obj;
            
            return Objects.equals(this.value, other.value);
        }        
    }
   
    
    /**
     * The map we are proxying.
     */
    private final Map<K,V> seed;

    private final Map<K,Modification<V>> modifications = new HashMap<>();
    
    public MapProxy(Map<K, V> seed) {
        this.seed = seed;
    }

    public void put(K key, V value ) {
        modifications.put(key, new PutValue<>(value));
    }
    
    public void remove(K key) {
        modifications.put(key, Modification.DELETE);
    }
    
    public V get( K key ) {
        if ( modifications.containsKey(key) ) {
            Modification<V> m = modifications.get(key);
            if ( m == Modification.DELETE ) {
                return null;
            } else {
                return ((PutValue<V>)m).value;
            }
        } else {
            return seed.get(key);
        }
    }
    
    public boolean has( K key ) {
        if ( modifications.containsKey(key) ) {
            Modification<V> m = modifications.get(key);
            return ( m != Modification.DELETE );
        } else {
            return seed.containsKey(key);
        }
    }

    public Set<K> keys() {
        Set<K> retVal = new HashSet<>(seed.keySet());
        modifications.forEach((k,v)->{
            if ( v == Modification.DELETE ) {
                retVal.remove(k);
            } else {
                retVal.add(k);
            }
        });
        return retVal;
    }
    
    // size
    public int size() { 
        return keys().size();
    }
    
    // get modifications
    public Map<K, Modification<V>> getModifications() {
        return modifications;
    }
     
    // clear modifications?
    public void reset() {
        modifications.clear();
    }
    
}
