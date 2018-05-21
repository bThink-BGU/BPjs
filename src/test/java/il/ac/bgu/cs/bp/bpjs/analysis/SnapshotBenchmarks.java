package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.analysis.bprogramio.BProgramSyncSnapshotIO;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Basic benchmarks in Snapshot cloner
 *
 * @author acepace
 */
public class SnapshotBenchmarks {

    private static String IMPLEMENTATION = "benchmarks/variableSizedArrays.js";
    private static String TEST_NAME = "variableSizedArrays.js";
    private static int INITIAL_ARRAY_SIZE = 1;
    private static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
    private static int NUM_STEPS = 10; // How many steps to take
    private static int OBJECT_TYPE = 0; //Set for integer
    private static int ITERATIONS = 5;

    public static void main(String[] args) throws Exception {

        BenchmarkResult integerResults = measureIntegerSizes();

        BenchmarkResult objectResults = measureObjectSize();
        outputBenchResults(integerResults);
        outputBenchResults(objectResults);
    }

    private static void outputBenchResults(BenchmarkResult result) throws IOException {
        result.outputMemoryStats();
        result.outputTimes();
        result.outputToCsv(Paths.get("."));
    }

    private static BenchmarkResult measureIntegerSizes() throws Exception {
        System.out.println("Measuring effect of integer size");
        return measureProgram(0);
    }

    private static BenchmarkResult measureObjectSize() throws Exception {
        System.out.println("Measuring effect of object size");
        return measureProgram(1);
    }

    private static BenchmarkResult measureProgram(int object_type) throws Exception {
        VisitedStateStore store = new BProgramStateVisitedStateStore();
        DfsBProgramVerifier verifier = new DfsBProgramVerifier();
        verifier.setVisitedNodeStore(store);

        /*
            Test for variable num_steps
            we want to see if there's a non linear increase in snapshot size
         */
        int[][] snapshotSet = new int[ITERATIONS][];
        long[][] verificationTimes = new long[ITERATIONS][];
        BProgram[] programs = new BProgram[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            System.out.printf("Measuring for array step of %d size\n", ARRAY_STEP + i);
            BProgram programToTest = makeBProgram(INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, object_type);
            programs[i] = programToTest;
            //have to clone the object because BPrograms are not reusable
            snapshotSet[i] = getStateSizes(makeBProgram(INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, OBJECT_TYPE));
            verificationTimes[i] = getVerificationTime(verifier,INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, object_type,ITERATIONS);

        }
        return new BenchmarkResult(TEST_NAME+"object",programs,snapshotSet, verificationTimes);
    }


    private static BProgram makeBProgram(int init_array_size, int array_step, int num_steps, int object_type) {
        // prepare b-program
        final BProgram bprog = new SingleResourceBProgram(IMPLEMENTATION);
        bprog.putInGlobalScope("INITIAL_ARRAY_SIZE", init_array_size);
        bprog.putInGlobalScope("ARRAY_STEP", array_step);
        bprog.putInGlobalScope("NUM_STEPS", num_steps);
        bprog.putInGlobalScope("OBJECT_TYPE", object_type);
        String name = String.format("%s_init_%d_stepSize_%d_numSteps_%d_type_%d", bprog.getName(), init_array_size, array_step, num_steps, object_type);
        bprog.setName(name);
        return bprog;
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

    private static long[] getVerificationTime( DfsBProgramVerifier vfr, int init_array_size, int array_step, int num_steps, int object_type, int iteration_count) {

        return LongStream.range(0, iteration_count).map(i -> {
            try {
                VerificationResult res = vfr.verify(makeBProgram(init_array_size,array_step,num_steps,object_type));
                return res.getTimeMillies();
            } catch (Exception e) {
                return 0;
            }
        }).toArray();
    }


    /**
     * This consoli
     */
    static class BenchmarkResult {
        final String benchName;
        final BProgram[] programs;
        final int[][] snapshotSizes;
        final long[][] verificationTimes;

        final String[] VERIFICATION_HEADERS = {"Run instance", "time"};
        final ArrayList<String> snapshot_headers = new ArrayList<>(Collections.singletonList("Run instance"));

        BenchmarkResult(String name, BProgram[] programs, int[][] snapshotSizes, long[][] verificationTimes) {
            this.benchName = name;
            this.programs = programs;
            this.snapshotSizes = snapshotSizes;
            this.verificationTimes = verificationTimes;
            //assume they're all identical, they better be
            IntStream.range(0, this.snapshotSizes[0].length).forEach(i -> snapshot_headers.add("raw " + i));
        }

        void outputMemoryStats() {
            outputMemoryStats(5);
        }

        void outputMemoryStats(int num_final_steps) {
            for (int i = NUM_STEPS - num_final_steps; i < NUM_STEPS; i++) {
                final int currentStep = i;
                IntSummaryStatistics stats = Arrays.stream(snapshotSizes).mapToInt(x -> x[currentStep]).summaryStatistics();
                int min = stats.getMin();
                int max = stats.getMax();
                double avg = stats.getAverage();
                long distinct = Arrays.stream(snapshotSizes).mapToInt(x -> x[currentStep]).distinct().count();
                System.out.printf("At step %d the memory values for the snapshots " +
                        "\n\tmin %d\n\tmax %d\n\tavg %f\n", i, min, max, avg);
                if (distinct != ITERATIONS) {
                    System.out.print("WARNING: Identical results despite different settings!\n");
                }
                System.out.println();
            }
        }

        void outputTimes() {
            for (int i = 0; i < verificationTimes.length; i++) {
                LongSummaryStatistics stats = Arrays.stream(verificationTimes[i]).summaryStatistics();
                long min = stats.getMin();
                long max = stats.getMax();
                double avg = stats.getAverage();
                System.out.printf("The verification stats for the program at test iteration %d are " + "\n\tmin %d\n\tmax %d\n\tavg %f\n", i, min, max, avg);
                System.out.println();
            }

        }


        /**
         * Outputs to benchName+X csv files of the raw memory and CPU times
         * @param basePath Directory where to write the results
         * @throws IOException in case of IO failure
         */
        void outputToCsv(Path basePath) throws IOException {

            outputTimesToCsv(basePath.resolve(benchName + "_times.csv"));
            outputSnapToCsv(basePath.resolve(benchName + "_memory.csv"));
        }

        void outputTimesToCsv(Path filePath) throws IOException {
            BufferedWriter out = Files.newBufferedWriter(filePath);
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                    .withHeader(VERIFICATION_HEADERS))) {
                for (int i = 0; i < verificationTimes.length; i++) {
                    Object[] toPrint = new Object[verificationTimes[i].length+1];
                    toPrint[0] = i;
                    for (int j = 0; j < verificationTimes[i].length; j++) {
                        toPrint[j+1] = verificationTimes[i][j];
                    }
                    printer.printRecord(toPrint);
                }
            }
        }

        void outputSnapToCsv(Path filePath) throws IOException {
            BufferedWriter out = Files.newBufferedWriter(filePath);
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                    .withHeader(snapshot_headers.toArray(new String[0])))) {
                for (int i = 0; i < this.snapshotSizes.length; i++) {
                    Object[] toPrint = new Object[snapshotSizes[i].length+1];
                    toPrint[0] = i;
                    for (int j = 0; j < snapshotSizes[i].length; j++) {
                        toPrint[j+1] = snapshotSizes[i][j];
                    }
                    printer.printRecord(toPrint);
                }
            }
        }
    }
}