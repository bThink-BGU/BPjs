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
 * An event selection strategy that:
 * <ol>
 * <li>Randomly selects an internal event that's requested and not blocked<li>
 * <li>If no such event is available, selects the first external event that's not blocked</li>
 * <li>If no such event is available, returns an empty result</li>
 * </ol>
 * 
 * Under this strategy, if the selected event is internal, and has {@code equal} events queued externally,
 * these events are <em>not</em> removed.
 * 
 * @author michael
 */
public class SimpleEventSelectionStrategy extends AbstractEventSelectionStrategy {
    
    
    public SimpleEventSelectionStrategy( long seed ) {
        super(seed);
    }
    
    public SimpleEventSelectionStrategy() {}
    
    
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
                .flatMap( stmt -> stmt.getRequest().stream() )
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
