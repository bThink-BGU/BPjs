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
package il.ac.bgu.cs.bp.bpjs.analysis.violations;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsTraversalNode;
import il.ac.bgu.cs.bp.bpjs.internal.Pair;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;

/**
 *
 * @author michael
 */
public class DeadlockViolation extends Violation {
    
    private final String description;
    
    public DeadlockViolation(List<DfsTraversalNode> counterExampleTrace) {
        super(counterExampleTrace);
        DfsTraversalNode last = counterExampleTrace.get(counterExampleTrace.size()-1);
        Map<BEvent, Set<String>> requestedBy = new HashMap<>();
        Map<BEvent, Set<String>> blockedBy;
        
        // collect who requested what
        last.getSystemState().getStatements().forEach( syncs -> {
            syncs.getRequest().forEach( evt -> {
                if ( ! requestedBy.containsKey(evt) ) {
                    requestedBy.put(evt, new HashSet<>());
                }
                requestedBy.get(evt).add(syncs.getBthread().getName());
            });
        });
        
        Context.enter();
        try {
            // collect who blocked what
            blockedBy = requestedBy.keySet().stream().map( evt -> Pair.of(
                    evt, 
                    last.getSystemState().getStatements().stream().filter(s->isBlocking(s,evt)).map(s->s.getBthread().getName()).collect(toSet()))
            ).collect( Collectors.toMap(p->p.getLeft(), p->p.getRight(), (s1, s2)->{
                Set<String> mergedSet = new TreeSet<>();
                mergedSet.addAll(s1);
                mergedSet.addAll(s2);
                return mergedSet;
            }) );
        } finally {
            Context.exit();
        }
        
        description = requestedBy.keySet().stream()
                .map( evt -> {
                    return evt.toString() + " requested by:" + setToString(requestedBy.get(evt))
                        + " blocked by:" + setToString(blockedBy.get(evt));
                }).sorted()
                .collect(joining("\n"));
                
                
    }

    private boolean isBlocking( SyncStatement stmt, BEvent evt ) {
        return stmt.getBlock().contains(evt);
    }
    
    private String setToString(Set<String> aSet){
        return aSet.stream().sorted().collect( joining(",","{","}"));
    }
    
    @Override
    public String decsribe() {
        return "Deadlock: " + description;
    }
    
}
