/*
 * The MIT License
 *
 * Copyright 2017 BGU.
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

import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;

/**
 * An event selection strategy that takes, for each statement, the first requested-and-not-blocked event.
 * So, for the following code, only event {@code evt1} will be considered for selection, assuming it is not
 * blocked by other b-threads.
 * 
 * <code>
 *  bsync({request:[evt1, evt2]...});
 * </code>
 * 
 * 
 * @author michael
 */
public class OrderedEventSelectionStrategy extends AbstractEventSelectionStrategy {

    @Override
    public Set<BEvent> selectableEvents(Set<BSyncStatement> statements, List<BEvent> externalEvents) {
        if ( statements.isEmpty() ) {
            // Corner case, not sure this is even possible.
            return externalEvents.isEmpty() ? emptySet() : singleton(externalEvents.get(0));
        }
        
        EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .map(BSyncStatement::getBlock )
                .filter(r -> r != EventSets.none )
                .collect( toSet() ) );
        
        Set<BEvent> requested = statements.stream()
                .filter( stmt -> stmt!=null )
                .map( stmt -> (BEvent)(stmt.getRequest().stream()
                                            .filter(e -> !blocked.contains(e))
                                            .findFirst().orElse(null))
                )
                .filter( e -> e != null)
                .collect( toSet() );
        
        // Let's see what internal events are requested and not blocked (if any).
        try {
            Context.enter();
            Set<BEvent> requestedAndNotBlocked = requested.stream()
                    .filter( req -> !blocked.contains(req) )
                    .collect( toSet() );

            return requestedAndNotBlocked.isEmpty() ?
                    externalEvents.stream().filter( e->!blocked.contains(e) ) // No internal events requested, defer to externals.
                                  .findFirst().map( e->singleton(e) ).orElse(emptySet())
                    : requestedAndNotBlocked;
        } finally {
            Context.exit();
        }
    }
    
}
