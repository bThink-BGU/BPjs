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
package il.ac.bgu.cs.bp.bpjs;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.mocks.MockBProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.StringBProgram;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Just a static place for some repeated methods useful for testing.
 * 
 * @author michael
 */
public abstract class TestUtils {
    
    /**
     * Preventing the instantiation of subclasses.
     */
    private TestUtils(){}
    
    public static String traceEventNamesString( VerificationResult res, String delimiter ) {
        return res.getViolation().map(v->traceEventNamesString(v.getCounterExampleTrace(), delimiter)).orElse("");
    }
    
    public static String traceEventNamesString( ExecutionTrace trace, String delimiter ) {
        return trace.getNodes().stream()
                    .map(ExecutionTrace.Entry::getEvent)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(BEvent::getName)
                    .collect(joining(delimiter));
    }
    
    public static List<String> traceEventNames( ExecutionTrace trace ) {
        return trace.getNodes().stream()
                    .map(ExecutionTrace.Entry::getEvent)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(BEvent::getName)
                    .collect(toList());
    }
    
    public static String eventNamesString( List<BEvent> trace, String delimiter ) {
        return trace.stream()
                    .map(BEvent::getName)
                    .collect(joining(delimiter));
    }
    
    public static <T> T safeGet( Future<T> f ) {
        try {
            return f.get();
        } catch (InterruptedException|ExecutionException ex) {
            System.out.println("Exception while calling safeGet:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
   
    public static BProgramSyncSnapshot makeBPSS( BThreadSyncSnapshot... snapshots ) {
        Set<BThreadSyncSnapshot> bts = new HashSet<>();
        bts.addAll(Arrays.asList(snapshots));
        return makeBPSS( bts );
    }
    
    public static BProgramSyncSnapshot makeBPSS( Collection<BThreadSyncSnapshot> snapshots ) {
        BProgram bprog = new StringBProgram("");
        Set<BThreadSyncSnapshot> bts = new HashSet<>(snapshots);
        return new MockBProgramSyncSnapshot(new BProgramSyncSnapshot(bprog, bts, Collections.emptyList(), null));
    }
    
    /**
     * Checks whether {@code sought} is equal to {@code list}, after the latter is
     * filtered to contain only members of former.
     * 
     * e.g.:
     * <code>
     * 1,2,3,4 is embedded in a,1,d,2,g,r,cv,3,g,4
     * a,b,x is not embedded in x,a,b
     * a,b,x is not embedded in a,a,b,x
     * </code>
     * 
     * @param <T>
     * @param sought
     * @param list
     * @return 
     */
    public static <T> boolean isEmbeddedSublist( List<T> sought, List<T> list ) {
        Set<T> filter = new HashSet<>(sought);
        return list.stream()
            .filter( filter::contains )
            .collect( toList() ).equals(sought);
    }
}
