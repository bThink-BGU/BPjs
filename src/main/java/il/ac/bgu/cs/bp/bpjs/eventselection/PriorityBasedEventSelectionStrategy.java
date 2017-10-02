package il.ac.bgu.cs.bp.bpjs.eventselection;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.mozilla.javascript.Context;

import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSets;

public class PriorityBasedEventSelectionStrategy implements EventSelectionStrategy {
    
    private final Random rnd;
    private final long seed;
    
    // A mapping of b-thread names to their priorities.
    private Map<String, Integer> priorities = new HashMap<String, Integer>();
    
    public PriorityBasedEventSelectionStrategy( long seed ) {
        rnd = new Random(seed);
        this.seed = seed;
    }
    
    public PriorityBasedEventSelectionStrategy() {
        rnd = new Random();
        seed = rnd.nextLong();
        rnd.setSeed(seed);
    }
    
    
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
        
        
        Set<Pair<BEvent,Integer>> requested = statements.stream()
                .filter( stmt -> stmt!=null )
                .flatMap( stmt -> stmt.getRequest().stream().map(e -> Pair.of(e, getPriority(stmt.getBthread().getName()))))
                .collect( Collectors.toSet() );
        
        // Let's see what internal events are requested and not blocked (if any).
        try {
            Context.enter();
            
            Set<Pair<BEvent,Integer>> requestedAndNotBlockedWithPriorities = requested.stream()
                    .filter( req -> !blocked.contains(req.getLeft()) )
                    .collect( toSet() );
            
            Integer highestPriority = requestedAndNotBlockedWithPriorities.stream().map(p -> p.getRight()).max(Integer::max).get();
            
            Set<BEvent> requestedAndNotBlocked = requestedAndNotBlockedWithPriorities.stream()
            	.filter(p -> p.getRight() == highestPriority)
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

    
    public void setPriority(String bThreadName, Integer priority) {
    	priorities.put(bThreadName, priority);
    }
    
    public Integer getPriority(String bThreadName) {
    	Integer p = priorities.get(bThreadName);
    	
    	if( p == null) 
    		return -1;
    	else
    		return p;
    }
    
    @Override
    public Optional<EventSelectionResult> select(Set<BSyncStatement> statements, List<BEvent> externalEvents, Set<BEvent> selectableEvents) {
        if ( selectableEvents.isEmpty() ) {
            return Optional.empty();
        }
        
        BEvent chosen = new ArrayList<>(selectableEvents).get(rnd.nextInt(selectableEvents.size()));

        
        Set<BEvent> requested = statements.stream()
                .filter( stmt -> stmt!=null )
                .flatMap( stmt -> stmt.getRequest().stream() )
                .collect( Collectors.toSet() );
        
        if (requested.contains(chosen)) {
            return Optional.of(new EventSelectionResult(chosen));
        } else {
            // that was an external event, need to find the first index 
            return Optional.of(new EventSelectionResult(chosen, singleton(externalEvents.indexOf(chosen))));
        }
    }
    
    public long getSeed() {
        return seed;
    }
}
