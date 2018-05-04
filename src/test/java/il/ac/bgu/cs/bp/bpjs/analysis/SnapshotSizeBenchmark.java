package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.analysis.bprogramio.BProgramSyncSnapshotIO;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Basic benchmarks in Snapshot cloner
 *
 * @author acepace
 */
public class SnapshotSizeBenchmark {

    private final static String IMPLEMENTATION = "benchmarks/variableSizedArrays.js";
    private static int INITIAL_ARRAY_SIZE = 1;
    private static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
    private static int NUM_STEPS = 10; // How many steps to take
    private static int OBJECT_TYPE = 0; //Set for integer
    private static int ITERATIONS = 20;

    public static void main(String[] args) throws Exception {
        int[][] intSnapshotSet = measureIntegerSizes();
        int[][] objSnapshotSet = measureObjectSize();
    }

    private static int[][] measureIntegerSizes() throws Exception {
        System.out.println("Measuring effect of integer size");
        /*
            Test for variable num_steps
            we want to see if there's a non linear increase in snapshot size
         */
        int[][] snapshotSet = new int[ITERATIONS][];
        for (int i = 0; i < ITERATIONS; i++) {
            System.out.printf("Measuring for array step of %d size\n", ARRAY_STEP + i);
            snapshotSet[i] = measureSnapshot(INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, OBJECT_TYPE);
        }

        /*  For each iteration test, instead of printing it out,
            we compare the final 5 steps across all the iterations and find min/max/average.
            Hopefully should be no diff
            TODO: output to a better log using opencsv
         */
        outputStats(snapshotSet);
        return snapshotSet;
    }

    private static int[][] measureObjectSize() throws Exception {
        System.out.println("Measuring effect of object size");
        /*
            Test for variable num_steps
            we want to see if there's a non linear increase in snapshot size
         */
        int[][] snapshotSet = new int[ITERATIONS][];
        for (int i = 0; i < ITERATIONS; i++) {
            System.out.printf("Measuring for array step of %d size\n", NUM_STEPS + i);
            snapshotSet[i] = measureSnapshot(INITIAL_ARRAY_SIZE, ARRAY_STEP, NUM_STEPS + i, 1);
        }

        /*  For each iteration test, instead of printing it out,
            we compare the final 5 steps across all the iterations and find min/max/average.
            Hopefully should be no diff
            TODO: output to a better log using opencsv
         */
        outputStats(snapshotSet);
        return snapshotSet;
    }

    private static void outputStats(int[][] snapshotSet) {
        for (int i = NUM_STEPS - 5; i < NUM_STEPS; i++) {
            final int currentStep = i;
            IntSummaryStatistics stats = Arrays.stream(snapshotSet).mapToInt(x -> x[currentStep]).summaryStatistics();
            int min = stats.getMin();
            int max = stats.getMax();
            double avg = stats.getAverage();
            long distinct = Arrays.stream(snapshotSet).mapToInt(x -> x[currentStep]).distinct().count();
            System.out.printf("At step %d the memory values for the snapshots " +
                    "\n\tmin %d\n\tmax %d\n\tavg %f\n", i, min, max, avg);
            if (distinct !=ITERATIONS ) {
                System.out.print("WARNING: Identical results despite different settings!\n");
            }
            System.out.println();
        }
    }

    private static BProgram makeBProgram(int init_array_size, int array_step, int num_steps, int object_type) {
        // prepare b-program
        final BProgram bprog = new SingleResourceBProgram(IMPLEMENTATION);
        bprog.putInGlobalScope("INITIAL_ARRAY_SIZE", init_array_size);
        bprog.putInGlobalScope("ARRAY_STEP", array_step);
        bprog.putInGlobalScope("NUM_STEPS", num_steps);
        bprog.putInGlobalScope("OBJECT_TYPE", object_type);
        return bprog;
    }

    private static int[] measureSnapshot(int init_array_size, int array_step, int num_steps, int object_type) throws Exception {
        /*for (int j = 0; j < snapshotSizes.length; j++) {
            System.out.printf("After %d syncs size was %d\n", j,snapshotSizes[j]);
        }*/
        return getStateSizes(makeBProgram(init_array_size, array_step, num_steps, object_type));
    }


    private static int[] getStateSizes(BProgram program) throws Exception {
        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("SnapshotStore");
        DfsBProgramVerifier sut = new DfsBProgramVerifier();
        List<Node> snapshots = new ArrayList<>();

        Node initial = Node.getInitialNode(program, execSvc);

        snapshots.add(initial);
        Node next = initial;
        //Iteration 1,starts already at request state A
        for (int i = 0; i < NUM_STEPS; i++) {
            next = sut.getUnvisitedNextNode(next, execSvc);
            snapshots.add(next);
        }
        BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(snapshots.get(0).getSystemState().getBProgram());
        return snapshots.stream().map(node -> {
            try {
                return io.serialize(node.getSystemState());
            } catch (IOException e) {
                return new byte[0];
            }
        }).mapToInt(serializedSnap -> serializedSnap.length).toArray();
    }
}