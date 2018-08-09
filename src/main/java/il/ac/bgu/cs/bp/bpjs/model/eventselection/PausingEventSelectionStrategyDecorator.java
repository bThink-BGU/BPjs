/*
 * The MIT License
 *
 * Copyright 2018 michael.
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
package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An event selection strategy decorator that pauses and waits for a callback
 * before selecting an event. The thread running the {@link BProgram} is put
 * blocked (in the Java sense) while it waits for the callback.
 * 
 * This class is thread-safe.
 * 
 * @author michael
 * @param <ESS> Subclass of {@link EventSelectionStrategy} being extended.
 */
public class PausingEventSelectionStrategyDecorator<ESS extends EventSelectionStrategy> extends AbstractEventSelectionStrategyDecorator<ESS> {
    
    public interface Listener {
        void eventSelectionPaused(PausingEventSelectionStrategyDecorator caller);
    }
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private Listener listener;
    
    public PausingEventSelectionStrategyDecorator(ESS decorated, Listener aListener) {
        super(decorated);
        listener = aListener;
    }
    public PausingEventSelectionStrategyDecorator(ESS decorated) {
        this(decorated, null);
    }

    @Override
    public Optional<EventSelectionResult> select(Set<BSyncStatement> statements, List<BEvent> externalEvents, Set<BEvent> selectableEvents) {
        Optional<EventSelectionResult> res = getDecorated().select(statements, externalEvents, selectableEvents);
        lock.readLock().lock();
        listener.eventSelectionPaused(this);
        lock.writeLock().lock(); // blocks while the read lock is locked.
        lock.writeLock().unlock();
        
        return res;
    }
    
    public void unpause() {
        lock.readLock().unlock();
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
    
}
