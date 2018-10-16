package il.ac.bgu.cs.bp.bpjs.model.eventselection;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.Context;

import il.ac.bgu.cs.bp.bpjs.model.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * An event selection strategy that prefers events from b-threads with higher
 * priorities. The higher the number assigned to a b-thread, the higher its
 * priority is.
 * 
 * @author geraw
 * @author michael
 */
public class PrioritizedBThreadsEventSelectionStrategy extends AbstractEventSelectionStrategy {

    public static final int DEFAULT_PRIORITY = -1;
    
    /** A mapping of b-thread names to their priorities. */
    final private Map<String, Integer> priorities = new HashMap<>();
    
    public PrioritizedBThreadsEventSelectionStrategy( long seed ) {
        super(seed);
    }
    
    public PrioritizedBThreadsEventSelectionStrategy() {
    }
    
    @Override
    public Set<BEvent> selectableEvents(Set<BSyncStatement> statements, List<BEvent> externalEvents) {
        if ( statements.isEmpty() ) {
            // Corner case, not sure this is even possible.
            return externalEvents.isEmpty() ? emptySet() : singleton(externalEvents.get(0));
        }
        
        final EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .map( BSyncStatement::getBlock )
                .filter( r -> r != EventSets.none )
                .collect( toSet() ) );
        
        Set<Pair<BEvent,Integer>> requested = statements.stream()
                .filter( stmt -> stmt!=null )
                .flatMap(stmt -> eventsToPrioritizedPairs(stmt))
                .collect( Collectors.toSet() );
        
        // Let's see what internal events are requested and not blocked (if any).
        try {
            Context.enter();
            
            Set<Pair<BEvent,Integer>> requestedAndNotBlockedWithPriorities = requested.stream()
                    .filter( req -> !blocked.contains(req.getLeft()) )
                    .collect( toSet() );
            
            Integer highestPriority = requestedAndNotBlockedWithPriorities.stream()
                .map(p -> p.getRight())
                .max(Comparator.comparing(Integer::valueOf)).get();
            
            Set<BEvent> requestedAndNotBlocked = requestedAndNotBlockedWithPriorities.stream()
            	.filter(p -> p.getRight().intValue() == highestPriority.intValue())
            	.map(p->p.getLeft())
            	.collect(toSet());

            return requestedAndNotBlocked.isEmpty() ?
                    externalEvents.stream().filter( e->!blocked.contains(e) ) // No internal events requested, defer to externals.
                                  .findFirst().map( e->singleton(e) ).orElse(emptySet())
                    : requestedAndNotBlocked;
        } finally {
            Context.exit();
        }
    }

    private Stream<Pair<BEvent, Integer>> eventsToPrioritizedPairs(BSyncStatement stmt) {
        final Collection<? extends BEvent> request = stmt.getRequest();
        if ( request.isEmpty() ) return Stream.empty();
        Integer priority = getPriority(stmt.getBthread().getName());
        return request.stream().map( e -> Pair.of(e, priority));
    }

    public void setPriority(String bThreadName, Integer priority) {
    	priorities.put(bThreadName, priority);
    }
    
    public int getPriority(String bThreadName) {
    	return priorities.getOrDefault(bThreadName, DEFAULT_PRIORITY);
    }
    
    public int getHighestPriority() {
        return priorities.values().stream().mapToInt( Integer::intValue ).max().orElse(DEFAULT_PRIORITY);
    }
    
    public int getLowestPriority() {
        return priorities.values().stream().mapToInt( Integer::intValue ).min().orElse(DEFAULT_PRIORITY);
    }
    
}
