/*
 * The MIT License
 *
 * Copyright 2019 michael.
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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.analysis.violations.DeadlockViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.FailedAssertionViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotBProgramCycleViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotBThreadCycleViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotTerminationViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * 
 * A static collection of commonly used inspections. These objects can detect
 * problems in a b-program, during a traversal of its state space.
 * 
 * @author michael
 */
public class ExecutionTraceInspections {
        
    /**
     * Detects a deadlock in a b-program: where there are requested events, but
     * non of them is selectable.
     */
    public static final ExecutionTraceInspection DEADLOCKS = ExecutionTraceInspection.named(
        "Deadlocks",
        trace -> {
            if ( trace.isCyclic() ) return Optional.empty(); 
            BProgramSyncSnapshot curState = trace.getLastState();
            if (hasRequestedEvents(curState) &&
                trace.getBProgram().getEventSelectionStrategy().selectableEvents(curState).isEmpty()) {
                return Optional.of(new DeadlockViolation(trace));
            } else return Optional.empty();
    });

    /**
     * Detects when a b-thread calls an assertion which evaluates to {@code false}.
     */    
    public static final ExecutionTraceInspection FAILED_ASSERTIONS = ExecutionTraceInspection.named(
        "Failed Assertions",
        trace -> {
            if ( trace.isCyclic() ) return Optional.empty(); 
            BProgramSyncSnapshot curState = trace.getLastState();
            if (!trace.getLastState().isStateValid()) {
                return Optional.of(new FailedAssertionViolation(trace.getLastState().getFailedAssertion(), trace));
            } else return Optional.empty();
    });
    
    /**
     * Detects a case where a b-program ends, while one of its b-threads is in a 
     * hot sync.
     */
    public static final ExecutionTraceInspection HOT_TERMINATIONS = ExecutionTraceInspection.named(
        "Hot Terminations",
        trace -> {
            if ( trace.isCyclic() ) return Optional.empty(); 
            BProgramSyncSnapshot curState = trace.getLastState();
            if ( curState.isHot() &&
                trace.getBProgram().getEventSelectionStrategy().selectableEvents(curState).isEmpty() ) {
                Set<String> hotlyTerminated = curState.getBThreadSnapshots().stream()
                    .filter( s -> s.getSyncStatement().isHot() ).map( s->s.getName() ).collect( toSet() );
                return Optional.of( new HotTerminationViolation(hotlyTerminated, trace) );
            } else return Optional.empty();
        }
     ); 
    
    /**
     * Detects a case where a b-program can get into an infinite loop where, at 
     * each sync point, at least one of the b-threads is in a hot sync. This does
     * not mean that individually b-threads are hot all along the loop.
     */
    public static final ExecutionTraceInspection HOT_BPROGRAM_CYCLES = ExecutionTraceInspection.named(
        "Hot B-Program Cycles",
        trace -> {
            if ( trace.isCyclic() &&
                 trace.getFinalCycle().stream().allMatch(s -> s.getState().isHot()) ){
                return Optional.of(new HotBProgramCycleViolation(trace));
            } else return Optional.empty();
        }
    );
    
    /**
     * Detects a case where a b-thread can get into an infinite loop where all
     * its sync points are hot.
     */    
    public static final ExecutionTraceInspection HOT_BTHREAD_CYCLES = new ExecutionTraceInspection(){
        @Override
        public String title() {
            return "Hot B-Program Cycles";
        }

        @Override
        public Optional<Violation> inspectTrace(ExecutionTrace trace) {
            if ( trace.isCyclic() &&
                  trace.getLastState().isHot() ) {
                List<ExecutionTrace.Entry> cycle = trace.getFinalCycle();
                List<Set<String>> hots = cycle.stream().map( nd -> nd.getState().getBThreadSnapshots() )
                                           .map( this::getHotThreadNames )
                                            .collect( toList() );
                
                Optional<Set<String>> alwaysHotOpt = hots.stream().reduce((s1, s2)->{
                    Set<String> retVal = new HashSet<>(s1);
                    retVal.retainAll(s2);
                    return retVal;
                });
                
                return alwaysHotOpt.filter( ahs -> ahs.size()>0 )
                    .map( alwaysHots -> new HotBThreadCycleViolation(trace, alwaysHots) );
                    
            } else return Optional.empty();
        }
        
         private Set<String> getHotThreadNames( Set<BThreadSyncSnapshot> bts ) {
            return bts.stream().filter( bt -> bt.getSyncStatement().isHot() )
                      .map( bt -> bt.getName() )
                      .collect( toSet() );
        }
    };
                
    
    public static final Set<ExecutionTraceInspection> DEFAULT_SET = Collections.unmodifiableSet( 
        new HashSet<ExecutionTraceInspection>(
            Arrays.asList(
                DEADLOCKS, FAILED_ASSERTIONS, HOT_TERMINATIONS, HOT_BTHREAD_CYCLES
    )));
    
    /////////////////////////
    // Utility methods.
    
    private static boolean hasRequestedEvents(BProgramSyncSnapshot bpss) {
        return bpss.getBThreadSnapshots().stream().anyMatch(btss -> (!btss.getSyncStatement().getRequest().isEmpty()));
    }
}
