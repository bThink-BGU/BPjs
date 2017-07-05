package il.ac.bgu.cs.bp.bpjs.examples;

import il.ac.bgu.cs.bp.bpjs.events.BEvent;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.BSyncStatement;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.BProgramListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.InMemoryEventLoggingListener;
import il.ac.bgu.cs.bp.bpjs.bprogram.runtimeengine.listeners.StreamLoggerListener;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.eventselection.EventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.eventsets.EventSets;
import il.ac.bgu.cs.bp.bpjs.validation.eventpattern.EventPattern;
import java.util.Collections;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toSet;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.mozilla.javascript.Context;

/**
 * @author @michbarsinai
 */
public class StatementsWithDataTest {

    @Test
    public void superStepTest() throws InterruptedException {
        SingleResourceBProgram bprog = new SingleResourceBProgram("StatementsWithData.js");
        BProgramRunner sut = new BProgramRunner();
        sut.addListener( new StreamLoggerListener() );
        InMemoryEventLoggingListener eventLogger = sut.addListener( new InMemoryEventLoggingListener() );
        sut.setBProgram(bprog);
        sut.getBProgram().setEventSelectionStrategy(new PriorityEss());
        sut.addListener( new BProgramListenerAdapter() {} );
        sut.start();
        
        eventLogger.getEvents().forEach(e->System.out.println(e) );

        EventPattern expected = new EventPattern()
                .append(new BEvent("1"))
                .append(new BEvent("2"))
                .append(new BEvent("3"));
        
        assertTrue( expected.matches(eventLogger.getEvents()) );
    }

}

class PriorityEss implements EventSelectionStrategy {

    @Override
    public Set<BEvent> selectableEvents(Set<BSyncStatement> statements, List<BEvent> externalEvents) {
        
         EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .map(BSyncStatement::getBlock )
                .filter(r -> r != EventSets.none )
                .collect( Collectors.toSet() ) );
         
        Iterator<BSyncStatement> stmts = statements.iterator();
        
        if ( stmts.hasNext() ) {
            BSyncStatement firstStmt = stmts.next();
            Set<BEvent> selectable = getNotBlocked(firstStmt, blocked);
            int minValue = getValue( firstStmt );
            
            while ( stmts.hasNext() ) {
                BSyncStatement curStmt = stmts.next();
                int curValue = getValue(curStmt);
                if ( curValue < minValue ) {
                    minValue = curValue;
                    selectable = getNotBlocked(curStmt, blocked);
                }
            }
            return new HashSet<>(selectable);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Optional<EventSelectionResult> select(Set<BSyncStatement> statements, List<BEvent> externalEvents, Set<BEvent> selectableEvents) {
        if ( selectableEvents.isEmpty() ) {
            return Optional.empty();
        } else {
            return Optional.of( new EventSelectionResult(selectableEvents.iterator().next()));
        }
    }
    
    private int getValue( BSyncStatement stmt ) {
        return stmt.hasData() ? ((Number)stmt.getData()).intValue() : Integer.MAX_VALUE;
    }

    private Set<BEvent> getNotBlocked(BSyncStatement stmt, EventSet blocked) {
        try {
            Context.enter();
            return stmt.getRequest().stream()
                    .filter( req -> !blocked.contains(req) )
                    .collect( toSet() );

        } finally {
            Context.exit();
        }
    }
    
}