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
package il.ac.bgu.cs.bp.bpjs.execution.jsproxy;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A type of {@link MapProxy} that directly modifies its map.Used for accessing
 the b-program data store during its initial execution phase (pre b-thread).
 * 
 * @author michael
 * @param <K>
 * @param <V>
 */
public class DirectMapProxy<K,V> extends MapProxy<K,V> {
    
    public DirectMapProxy(Map<K, V> aSeed) {
        super(aSeed);
    }

    @Override
    public int size() {
        return seed.size();
    }

    @Override
    public Set<K> keys() {
        return seed.keySet();
    }

    @Override
    public boolean has(K key) {
        return seed.containsKey(key);
    }

    @Override
    public V get(K key) {
        return seed.get(key);
    }

    @Override
    public Map<K, V> filter(BiFunction<K, V, Boolean> func) {
        return seed.entrySet().stream()
            .filter(entry -> func.apply(entry.getKey(), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Collection<V> values() {
        return seed.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return seed.entrySet();
    }

    @Override
    public void remove(K key) {
        seed.remove(key);
    }

    @Override
    public void put(K key, V value) {
        seed.put(key, value);
    }
    
}
