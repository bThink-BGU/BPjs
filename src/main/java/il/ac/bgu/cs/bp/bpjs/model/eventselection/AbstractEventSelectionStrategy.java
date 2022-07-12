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

import il.ac.bgu.cs.bp.bpjs.BPjs;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import java.util.ArrayList;
import static java.util.Collections.singleton;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;

/**
 * A convenience class for implementing event selection strategies. Contains mostly reusable code for randomizing selectable events.
 * 
 * 
 * @author michael
 */
public abstract class AbstractEventSelectionStrategy implements EventSelectionStrategy {

    protected final Random rnd;
    protected final long seed;
    
    public AbstractEventSelectionStrategy( long seed ) {
        rnd = new Random(seed);
        this.seed = seed;
    }
    
    public AbstractEventSelectionStrategy() {
        rnd = new Random();
        seed = rnd.nextLong();
        rnd.setSeed(seed);
    }

    public long getSeed() {
        return seed;
    }

    /**
     * Randomly select an event from {@code selectableEvents}, or a non-blocked event from {@code externalEvents}, in case {@code selectableEvents} is empty.
     * 
     * @param bpss a b-program at synchronization point.
     * @param selectableEvents Set of events that can be selected.
     * @return An optional event selection result.
     */
    @Override
    public Optional<EventSelectionResult> select(BProgramSyncSnapshot bpss, Set<BEvent> selectableEvents) {
        if (selectableEvents.isEmpty()) {
            return Optional.empty();
        }
        BEvent chosen = new ArrayList<>(selectableEvents).get(rnd.nextInt(selectableEvents.size()));
        Set<BEvent> requested = bpss.getStatements().stream()
                                          .filter((SyncStatement stmt) -> stmt != null)
                                          .flatMap((SyncStatement stmt) -> stmt.getRequest().stream())
                                          .collect(Collectors.toSet());
        
        if (requested.contains(chosen)) {
            return Optional.of(new EventSelectionResult(chosen));
        } else {
            // that was an external event, need to find the first index
            return Optional.of(new EventSelectionResult(chosen, singleton(bpss.getExternalEvents().indexOf(chosen))));
        }
    }

    protected Set<BEvent> getRequestedAndNotBlocked(SyncStatement stmt, EventSet blocked) {
        try (Context curCtx = BPjs.enterRhinoContext()) {
            return stmt.getRequest().stream().filter((BEvent req) -> !blocked.contains(req)).collect(toSet());
        }
    }
    
    private boolean warningIssued = false;
    
    /**
     * Log a warning when b-threads send synchronization statement data. This 
     * is useful for strategies that ignore these data, as users might be sending
     * them and not understand why they are ignored.
     * 
     * @param bpss the state the b-program is at.
     */
    protected void warnOnHints( BProgramSyncSnapshot bpss ){
        if ( warningIssued ) return; // warn only once
        if ( bpss.getStatements().stream().anyMatch( s->s.getData()!=null ) ) {
            System.out.println("[WARNING] Sync statements contain data. Current strategy ("+getClass().getCanonicalName()+") ignores this field.");
            warningIssued = true;
        }
    }
    
}
