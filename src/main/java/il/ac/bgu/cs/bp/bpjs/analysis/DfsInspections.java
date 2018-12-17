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
package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.analysis.violations.DeadlockViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.FailedAssertionViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotCycleViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.HotTerminationViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static java.util.stream.Collectors.toSet;

/**
 *
 * A static collection of {@link DfsInspection}s, commonly used 
 * in verifying b-programs.
 * 
 * @author michael
 */
public final class DfsInspections {
    
    public static final DfsTraceInspection Deadlock = (List<DfsTraversalNode> trace) -> {
        DfsTraversalNode curNode = peek(trace);
        if (hasRequestedEvents(curNode.getSystemState()) &&
            curNode.getSelectableEvents().isEmpty()) {
            return Optional.of(new DeadlockViolation(trace));
        } else return Optional.empty();
    };
    
    public static final DfsTraceInspection FailedAssertions = (List<DfsTraversalNode> trace) -> {
        DfsTraversalNode curNode = peek(trace);
        if (!curNode.getSystemState().isStateValid()) {
            return Optional.of(new FailedAssertionViolation(curNode.getSystemState().getFailedAssertion(), trace));
        } else return Optional.empty();
    };
        
    public static final DfsTraceInspection HotTermination = (List<DfsTraversalNode> trace) -> {
        DfsTraversalNode curNode = peek(trace);
        if ( curNode.getSystemState().isHot() &&
            curNode.getSelectableEvents().isEmpty() ) {
            Set<String> hotlyTerminated = curNode.getSystemState().getBThreadSnapshots().stream()
                .filter( s -> s.getBSyncStatement().isHot() ).map( s->s.getName() ).collect( toSet() );
            return Optional.of( new HotTerminationViolation(hotlyTerminated, trace) );
        } else return Optional.empty();
    }; 
    
    public static final DfsCycleInspection HotCycles = (List<DfsTraversalNode> currentTrace, int cycleToIdx, BEvent event) -> {
        if ( currentTrace.subList(cycleToIdx, currentTrace.size()).stream().allMatch(s -> s.getSystemState().isHot()) ){
            return Optional.of( new HotCycleViolation(currentTrace, cycleToIdx, event));
        } else return Optional.empty();
    };
    
    public static final List<DfsTraceInspection> ALL_TRACE = Arrays.asList(
        DfsInspections.Deadlock,
        DfsInspections.FailedAssertions,
        DfsInspections.HotTermination
    );
            
    /////////////////////////
    // Utility methods.
    
    private static boolean hasRequestedEvents(BProgramSyncSnapshot bpss) {
        return bpss.getBThreadSnapshots().stream().anyMatch(btss -> (!btss.getBSyncStatement().getRequest().isEmpty()));
    }
    
    private static DfsTraversalNode peek(List<DfsTraversalNode> trace) {
        return trace.isEmpty() ? null : trace.get(trace.size()-1);
    }
    

    // / Utility methods.
    /////////////////////////
    
    
    // Prevent instantiation.
    private DfsInspections(){}
    
}
