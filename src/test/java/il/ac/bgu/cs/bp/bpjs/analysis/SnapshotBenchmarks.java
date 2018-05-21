package il.ac.bgu.cs.bp.bpjs.analysis;

import il.ac.bgu.cs.bp.bpjs.analysis.bprogramio.BProgramSyncSnapshotIO;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.SingleResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.*;
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


    private static int ITERATIONS = 10;

    public static void main(String[] args) throws Exception {

        VariableSizedArraysBenchmark.benchmarkVariableSizedArrays();
        EventSelectionBenchmark.benchmarkEventSelection();
    }


    /*
        This benchmarks a program of variableSizedArrays
        For verification time benchmarks, each test is run ITERATIONS times, with a larger array step every time.
            These should be pretty much identical
        For snapshot size benchmarks, each test is run once, and we measure the snapshot size for each step.
            Snapshot size should grow linearly.
     */
    static class VariableSizedArraysBenchmark {
        private static String IMPLEMENTATION = "benchmarks/variableSizedArrays.js";
        private static String TEST_NAME = "variableSizedArrays";
        private static int INITIAL_ARRAY_SIZE = 1;
        private static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
        private static int NUM_STEPS = 10; // How many steps to take


        static void benchmarkVariableSizedArrays() throws Exception {
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
            VisitedStateStore store = new BThreadSnapshotVisitedStateStore();
            VisitedStateStore storeHash = new HashVisitedStateStore();
            DfsBProgramVerifier verifier = new DfsBProgramVerifier();
            String testName = TEST_NAME + ((object_type == 1) ? "object" : "integer");
            /*
                Test for variable num_steps
                we want to see if there's a non linear increase in snapshot size
             */
            int[][] snapshotSet = new int[ITERATIONS][];
            long[][] verificationTimes = new long[ITERATIONS][];
            long[][] verificationTimesHash = new long[ITERATIONS][];
            BProgram[] programs = new BProgram[ITERATIONS];
            for (int i = 0; i < ITERATIONS; i++) {
                System.out.printf("Measuring for array step of %d size\n", ARRAY_STEP + i);
                BProgram programToTest = makeBProgram(INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, object_type);
                programs[i] = programToTest;
                //Cleanup GC before and after, opportunistic
                //have to clone the object because BPrograms are not reusable
                System.gc();
                snapshotSet[i] = getStateSizes(makeBProgram(INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, object_type));
                System.gc();
                verifier.setVisitedNodeStore(store);
                verificationTimes[i] = getVerificationTime(verifier, INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, object_type, ITERATIONS);
                verifier.setVisitedNodeStore(storeHash);
                verificationTimesHash[i] = getVerificationTime(verifier, INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, object_type, ITERATIONS);
                System.gc();

            }

            return new BenchmarkResult(testName, programs, snapshotSet, verificationTimes, verificationTimesHash);
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
            BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(program);
            execSvc.shutdown();

            return snapshots.stream().map(node -> {
                try {
                    return io.serialize(node.getSystemState());
                } catch (IOException e) {
                    return new byte[0];
                }
            }).mapToInt(serializedSnap -> serializedSnap.length).toArray();

        }

        private static long[] getVerificationTime(DfsBProgramVerifier vfr, int init_array_size, int array_step, int num_steps, int object_type, int iteration_count) {

            return LongStream.range(0, iteration_count).map(i -> {
                try {
                    VerificationResult res = vfr.verify(makeBProgram(init_array_size, array_step, num_steps, object_type));
                    return res.getTimeMillies();
                } catch (Exception e) {
                    return 0;
                }
            }).toArray();
        }


    }

    static class EventSelectionBenchmark {
        private static String IMPLEMENTATION = "benchmarks/eventSelection.js";
        private static String TEST_NAME = "eventSelection";
        private static int INITIAL_ARRAY_SIZE = 1;
        private static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
        private static int NUM_STEPS = 10; // How many steps to take

        static void benchmarkEventSelection() throws Exception {
            BenchmarkResult simpleEventSelectionResults = measureProgram(new SimpleEventSelectionStrategy());
            BenchmarkResult orderedEventSelectionResults = measureProgram(new OrderedEventSelectionStrategy());
            //BenchmarkResult PrioritizedBSyncEventSelectionResults = measureProgram(new PrioritizedBSyncEventSelectionStrategy());
            //BenchmarkResult PrioritizedBThreadsEventSelectionResults = measureProgram(new PrioritizedBThreadsEventSelectionStrategy());

            outputBenchResults(simpleEventSelectionResults);
            outputBenchResults(orderedEventSelectionResults);
        }


        private static void outputBenchResults(BenchmarkResult result) throws IOException {
            //result.outputMemoryStats();
            result.outputTimes();
            result.outputToCsv(Paths.get("."));
        }

        private static BenchmarkResult measureProgram(EventSelectionStrategy strategy) throws Exception {
            VisitedStateStore store = new BThreadSnapshotVisitedStateStore();
            DfsBProgramVerifier verifier = new DfsBProgramVerifier();
            verifier.setVisitedNodeStore(store);

            /*
                Test for variable num_steps
                we want to see if there's a non linear increase in snapshot size
             */
            int[][] snapshotSet = new int[ITERATIONS][];
            long[][] verificationTimes = new long[ITERATIONS][];
            long[][] verificationTimesHash = new long[ITERATIONS][];
            BProgram[] programs = new BProgram[ITERATIONS];
            for (int i = 0; i < ITERATIONS; i++) {
                System.out.printf("Measuring for array step of %d size\n", ARRAY_STEP + i);
                //Initial copy
                BProgram programToTest = makeBProgram(INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, strategy);
                programs[i] = programToTest;
                //Cleanup GC before and after, opportunistic
                //have to clone the program because BPrograms are not reusable
                System.gc();
                snapshotSet[i] = getStateSizes(makeBProgram(INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, strategy));
                System.gc();
                verificationTimes[i] = getVerificationTime(verifier, INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, strategy, ITERATIONS);
                verificationTimesHash[i] = getVerificationTime(verifier, INITIAL_ARRAY_SIZE, ARRAY_STEP + i, NUM_STEPS, strategy, ITERATIONS);
                System.gc();

            }

            return new BenchmarkResult(TEST_NAME+strategy.getClass().getName(), programs, snapshotSet, verificationTimes, verificationTimesHash);
        }


        private static BProgram makeBProgram(int init_array_size, int array_step, int num_steps, EventSelectionStrategy strategy) {
            // prepare b-program
            final BProgram bprog = new SingleResourceBProgram(IMPLEMENTATION);
            bprog.putInGlobalScope("INITIAL_ARRAY_SIZE", init_array_size);
            bprog.putInGlobalScope("ARRAY_STEP", array_step);
            bprog.putInGlobalScope("NUM_STEPS", num_steps);
            String name = String.format("%s_strategy_%s_init_%d_stepSize_%d_numSteps_%d", bprog.getName(), strategy.getClass().getName(), init_array_size, array_step, num_steps);
            bprog.setName(name);
            bprog.setEventSelectionStrategy(strategy);
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
            BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(program);
            execSvc.shutdown();

            return snapshots.stream().map(node -> {
                try {
                    return io.serialize(node.getSystemState());
                } catch (IOException e) {
                    return new byte[0];
                }
            }).mapToInt(serializedSnap -> serializedSnap.length).toArray();

        }

        private static long[] getVerificationTime(DfsBProgramVerifier vfr, int init_array_size, int array_step, int num_steps, EventSelectionStrategy strategy, int iteration_count) {

            return LongStream.range(0, iteration_count).map(i -> {
                try {
                    VerificationResult res = vfr.verify(makeBProgram(init_array_size, array_step, num_steps, strategy));
                    return res.getTimeMillies();
                } catch (Exception e) {
                    return 0;
                }
            }).toArray();
        }
    }

    /**
     * This consolidates benchmark results, not generic enough yet
     */
    static class BenchmarkResult {
        final String benchName;
        final BProgram[] programs;
        final int[][] snapshotSizes;
        final long[][] verificationTimesNoHash;
        final long[][] verificationTimesHash;

        final ArrayList<String> verificationHeaders = new ArrayList<>(Collections.singletonList("Iteration count"));
        final ArrayList<String> snapshot_headers = new ArrayList<>(Collections.singletonList("Run instance"));

        BenchmarkResult(String name, BProgram[] programs, int[][] snapshotSizes, long[][] verificationTimesNoHash, long[][] verificationTimesHash) {
            this.benchName = name;
            this.programs = programs;
            this.snapshotSizes = snapshotSizes;
            this.verificationTimesNoHash = verificationTimesNoHash;
            this.verificationTimesHash = verificationTimesHash;
            //assume they're all identical, they better be
            IntStream.range(0, this.snapshotSizes[0].length).forEach(i -> snapshot_headers.add("raw " + i));
            IntStream.range(0, this.verificationTimesNoHash[0].length).forEach(i -> verificationHeaders.add("raw " + i));
        }


        /**
         * Outputs memory statistics only for the last 5 steps.
         */
        void outputMemoryStats() {
            outputMemoryStats(5);
        }

        void outputMemoryStats(int num_final_steps) {
            for (int i = snapshotSizes[0].length - num_final_steps; i < snapshotSizes[0].length; i++) {
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
            for (int i = 0; i < verificationTimesNoHash.length; i++) {
                LongSummaryStatistics stats = Arrays.stream(verificationTimesNoHash[i]).summaryStatistics();
                long min = stats.getMin();
                long max = stats.getMax();
                double avg = stats.getAverage();
                System.out.printf("The verification stats for the program at test iteration %d are " + "\n\tmin %d\n\tmax %d\n\tavg %f\n", i, min, max, avg);
                System.out.println();
            }

        }


        /**
         * Outputs to benchName+X csv files of the raw memory and CPU times
         *
         * @param basePath Directory where to write the results
         * @throws IOException in case of IO fחailure
         */
        void outputToCsv(Path basePath) throws IOException {

            outputTimesToCsv(basePath.resolve(benchName + "_times.csv"), verificationTimesNoHash);
            outputTimesToCsv(basePath.resolve(benchName + "_timesHash.csv"), verificationTimesHash);
            outputSnapToCsv(basePath.resolve(benchName + "_memory.csv"));
        }

        void outputTimesToCsv(Path filePath, long[][] verificationTimes) throws IOException {
            BufferedWriter out = Files.newBufferedWriter(filePath);
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                    .withHeader(verificationHeaders.toArray(new String[0])))) {
                for (int i = 0; i < verificationTimes.length; i++) {
                    Object[] toPrint = new Object[verificationTimes[i].length + 1];
                    toPrint[0] = i;
                    for (int j = 0; j < verificationTimes[i].length; j++) {
                        toPrint[j + 1] = verificationTimes[i][j];
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
                    Object[] toPrint = new Object[snapshotSizes[i].length + 1];
                    toPrint[0] = i;
                    for (int j = 0; j < snapshotSizes[i].length; j++) {
                        toPrint[j + 1] = snapshotSizes[i][j];
                    }
                    printer.printRecord(toPrint);
                }
            }
        }
    }
}