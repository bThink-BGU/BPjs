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
package il.ac.bgu.cs.bp.bpjs.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A set, where the iteration order is the order of addition.
 *
 * @author michael
 * @param <T> Element type
 */
public class OrderedSet<T> implements SortedSet<T>, java.io.Serializable {

    private final ArrayList<T> items;

    public static <T> OrderedSet<T> of(T... ts) {
        OrderedSet<T> retVal = new OrderedSet<>();
        Arrays.stream(ts).forEach(retVal::add);
        return retVal;
    }

    public OrderedSet() {
        items = new ArrayList<>();
    }

    public OrderedSet(Collection<T> someItems) {
        items = new ArrayList<>(someItems.size());

        addAll(someItems);
    }

    @Override
    public Comparator<? super T> comparator() {
        return null;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        int idxFrom = items.indexOf(fromElement);
        int idxTo = items.indexOf(toElement);
        return Collections.unmodifiableSortedSet(new OrderedSet<>(items.subList(idxFrom, idxTo)));
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        int idxTo = items.indexOf(toElement);
        return (idxTo == -1) ? Collections.emptySortedSet()
                : Collections.unmodifiableSortedSet(new OrderedSet<>(items.subList(0, idxTo)));
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        int idxFrom = items.indexOf(fromElement);
        return (idxFrom == -1) ? Collections.emptySortedSet()
                : Collections.unmodifiableSortedSet(new OrderedSet<>(items.subList(idxFrom, items.size())));
    }

    @Override
    public T first() {
        return items.get(0);
    }

    @Override
    public T last() {
        return items.get(items.size() - 1);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return items.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return items.toArray(a);
    }

    @Override
    public boolean add(T e) {
        if (!items.contains(e)) {
            items.add(e);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean remove(Object o) {
        if (items.contains(o)) {
            items.remove(o);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return items.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean added = false;
        for (T t : c) {
            if (!contains(t)) {
                added = true;
                items.add(t);
            }
        }
        return added;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return items.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return items.removeAll(c);
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return SortedSet.super.removeIf(filter);
    }

    @Override
    public Stream<T> stream() {
        return SortedSet.super.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return SortedSet.super.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        SortedSet.super.forEach(action);
    }

    @Override
    public String toString() {
        return "[OrderedSet " + items.toString() + "]";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.items);
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
        if (obj instanceof Set) {
            Set otherSet = (Set) obj;
            if (obj instanceof OrderedSet) {
                OrderedSet otherOrderedSet = (OrderedSet) otherSet;
                return items.equals(otherOrderedSet.items);

            } else {
                // regular set equality
                return containsAll(otherSet) && otherSet.containsAll(this);
            }

        } else {
            return false;
        }
    }

}
