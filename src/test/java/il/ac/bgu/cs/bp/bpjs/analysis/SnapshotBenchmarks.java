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

    static abstract class Benchmark {
        static String IMPLEMENTATION = "TESTFILE.js";
        static String TEST_NAME = "TESTNAME";
        static int INITIAL_ARRAY_SIZE = 1;
        static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
        static int NUM_STEPS = 10; // How many steps to take


        static void outputBenchResults(BenchmarkResult result) throws IOException {
            result.outputMemoryStats();
            result.outputTimes();
            result.outputToCsv(Paths.get("."));
        }

        static BProgram makeBProgram(String baseFile, Map<String, Object> init_values) {
            final BProgram bprog = new SingleResourceBProgram(baseFile);
            init_values.forEach(bprog::putInGlobalScope);
            StringBuilder name = new StringBuilder(bprog.getName());
            init_values.forEach((k, v) -> name.append(String.format("_%s_%s", k, v)));
            bprog.setName(name.toString());
            return bprog;
        }


        static int[] getStateSizes(BProgram program) throws Exception {
            System.out.println("Measuring state size");
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

        static long getVerificationTime(DfsBProgramVerifier vfr, BProgram prog) {
            try {
                VerificationResult res = vfr.verify(prog);
                return res.getTimeMillies();
            } catch (Exception e) {
                return 0;
            }
        }

        static long[] getVerificationTime(DfsBProgramVerifier vfr, String programPath, Map<String, Object> valueMap, int iteration_count) {
            return LongStream.range(0, iteration_count).map(i -> {
                BProgram prog = makeBProgram(programPath, valueMap);
                return getVerificationTime(vfr, prog);
            }).toArray();
        }
    }

    /*
        This benchmarks a program of variableSizedArrays
        For verification time benchmarks, each test is run ITERATIONS times, with a larger array step every time.
            These should be pretty much identical
        For snapshot size benchmarks, each test is run once, and we measure the snapshot size for each step.
            Snapshot size should grow linearly.
     */
    static class VariableSizedArraysBenchmark extends Benchmark {
        static String IMPLEMENTATION = "benchmarks/variableSizedArrays.js";
        static String TEST_NAME = "variableSizedArrays";
        static int INITIAL_ARRAY_SIZE = 1;
        static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
        static int NUM_STEPS = 10; // How many steps to take


        static void benchmarkVariableSizedArrays() throws Exception {
            BenchmarkResult integerResults = measureIntegerSizes();

            BenchmarkResult objectResults = measureObjectSize();
            outputBenchResults(integerResults);
            outputBenchResults(objectResults);
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
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("INITIAL_ARRAY_SIZE", INITIAL_ARRAY_SIZE);
                valueMap.put("ARRAY_STEP", ARRAY_STEP + i);
                valueMap.put("NUM_STEPS", NUM_STEPS);
                valueMap.put("OBJECT_TYPE", object_type);

                BProgram programToTest = makeBProgram(IMPLEMENTATION, valueMap);
                programs[i] = programToTest;
                //Cleanup GC before and after, opportunistic
                //have to clone the object because BPrograms are not reusable
                System.gc();
                snapshotSet[i] = getStateSizes(makeBProgram(IMPLEMENTATION, valueMap));
                System.gc();
                verifier.setVisitedNodeStore(store);
                verificationTimes[i] = getVerificationTime(verifier, IMPLEMENTATION, valueMap, ITERATIONS);
                verifier.setVisitedNodeStore(storeHash);
                verificationTimesHash[i] = getVerificationTime(verifier, IMPLEMENTATION, valueMap, ITERATIONS);
                System.gc();

            }

            return new BenchmarkResult(testName, programs, snapshotSet, verificationTimes, verificationTimesHash);
        }

    }

    static class EventSelectionBenchmark extends Benchmark {
        static String IMPLEMENTATION = "benchmarks/eventSelection.js";
        static String TEST_NAME = "eventSelection";
        static int INITIAL_ARRAY_SIZE = 1;
        static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
        static int NUM_STEPS = 10; // How many steps to take

        static void benchmarkEventSelection() throws Exception {
            BenchmarkResult simpleEventSelectionResults = measureProgram(new SimpleEventSelectionStrategy());
            BenchmarkResult orderedEventSelectionResults = measureProgram(new OrderedEventSelectionStrategy());
            //BenchmarkResult PrioritizedBSyncEventSelectionResults = measureProgram(new PrioritizedBSyncEventSelectionStrategy());
            //BenchmarkResult PrioritizedBThreadsEventSelectionResults = measureProgram(new PrioritizedBThreadsEventSelectionStrategy());

            outputBenchResults(simpleEventSelectionResults);
            outputBenchResults(orderedEventSelectionResults);
        }


        private static BenchmarkResult measureProgram(EventSelectionStrategy strategy) throws Exception {
            VisitedStateStore store = new BThreadSnapshotVisitedStateStore();
            VisitedStateStore storeHash = new HashVisitedStateStore();
            DfsBProgramVerifier verifier = new DfsBProgramVerifier();

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

                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("INITIAL_ARRAY_SIZE", INITIAL_ARRAY_SIZE);
                valueMap.put("ARRAY_STEP", ARRAY_STEP + i);
                valueMap.put("NUM_STEPS", NUM_STEPS);

                //Initial copy
                BProgram programToTest = makeBProgram(IMPLEMENTATION, valueMap, strategy);
                programs[i] = programToTest;
                //Cleanup GC before and after, opportunistic
                //have to clone the program because BPrograms are not reusable
                System.gc();
                snapshotSet[i] = getStateSizes(makeBProgram(IMPLEMENTATION, valueMap, strategy));
                System.gc();
                verifier.setVisitedNodeStore(store);
                verificationTimes[i] = getVerificationTime(verifier, IMPLEMENTATION, valueMap, ITERATIONS, strategy);
                verifier.setVisitedNodeStore(storeHash);
                verificationTimesHash[i] = getVerificationTime(verifier, IMPLEMENTATION, valueMap, ITERATIONS, strategy);
                System.gc();

            }

            return new BenchmarkResult(TEST_NAME + strategy.getClass().getName(), programs, snapshotSet, verificationTimes, verificationTimesHash);
        }


        private static BProgram makeBProgram(String programPath, Map<String, Object> value_map, EventSelectionStrategy strategy) {
            // prepare b-program
            BProgram bprog = makeBProgram(programPath, value_map);
            bprog.setEventSelectionStrategy(strategy);
            return bprog;
        }

        static long[] getVerificationTime(DfsBProgramVerifier vfr, String programPath, Map<String, Object> valueMap, int iteration_count, EventSelectionStrategy strategy) {
            return LongStream.range(0, iteration_count).map(i -> {
                BProgram prog = makeBProgram(programPath, valueMap, strategy);
                return getVerificationTime(vfr, prog);
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