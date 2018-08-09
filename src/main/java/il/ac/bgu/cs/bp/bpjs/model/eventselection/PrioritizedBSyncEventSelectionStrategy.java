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
package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;

/**
 * An event selection strategy that prefers events requested by BSync statements with higher priority. 
 * BSync statement priority is determined by an integer added to the BSync metadata field, like so:
 * 
 * <code>
 * bsync({ request:..., waitFor:... }, 2);
 * </code>
 * 
 * @author michael
 */
public class PrioritizedBSyncEventSelectionStrategy extends AbstractEventSelectionStrategy {

    public PrioritizedBSyncEventSelectionStrategy(long seed) {
        super(seed);
    }

    public PrioritizedBSyncEventSelectionStrategy() {
    }
    
    @Override
    public Set<BEvent> selectableEvents(Set<BSyncStatement> statements, List<BEvent> externalEvents) {
        
        EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .map( BSyncStatement::getBlock )
                .filter( r -> r != EventSets.none )
                .collect( Collectors.toSet() ) );
        
        OptionalInt maxValueOpt = statements.stream()
                                            .filter( s -> !getRequestedAndNotBlocked(s, blocked).isEmpty() )
                                            .mapToInt(this::getValue)
                                            .max();
        
        try {
            Context.enter();
            if ( maxValueOpt.isPresent() ) {
                int maxValue = maxValueOpt.getAsInt();
                return statements.stream().filter( s -> getValue(s) == maxValue )
                                 .flatMap( s -> getRequestedAndNotBlocked(s, blocked).stream() )
                                 .collect( toSet() );
            } else {
                // Can't select any internal event, defer to the external, non-blocked ones.
                return externalEvents.stream().filter( e->!blocked.contains(e) ) // No internal events requested, defer to externals.
                                      .findFirst().map( e->singleton(e) ).orElse(emptySet());
            }
        } finally {
            Context.exit();
        }
       
    }
    
    private int getValue( BSyncStatement stmt ) {
        return (stmt.hasData() && (stmt.getData() instanceof Number))? 
                ((Number)stmt.getData()).intValue() : Integer.MIN_VALUE;
    }

    
}