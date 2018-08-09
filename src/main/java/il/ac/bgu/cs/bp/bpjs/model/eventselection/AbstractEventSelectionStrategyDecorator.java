/*
 * The MIT License
 *
 * Copyright 2018 BGU-BPJS team.
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
import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Base class for {@link EventSelectionStrategy} decorators.These allow
 modification/inspection of existing event selection strategies via 
 composition rather than subclassing.
 * 
 * @author michael
 * @param <ESS> The type of event selection strategy being decorated.
 */
public abstract class AbstractEventSelectionStrategyDecorator<ESS extends EventSelectionStrategy> implements EventSelectionStrategy {
    
    protected final ESS decorated;

    public AbstractEventSelectionStrategyDecorator(ESS decorated) {
        this.decorated = decorated;
    }
    
    public ESS getDecorated() {
        return decorated;
    }

    @Override
    public Set<BEvent> selectableEvents(Set<BSyncStatement> statements, List<BEvent> externalEvents) {
        return getDecorated().selectableEvents(statements, externalEvents);
    }

    @Override
    public Optional<EventSelectionResult> select(Set<BSyncStatement> statements, List<BEvent> externalEvents, Set<BEvent> selectableEvents) {
        return getDecorated().select(statements, externalEvents, selectableEvents);
    }
    
}
