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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Basic benchmarks in Snapshot cloner
 *
 * @author acepace
 */
public class SnapshotBenchmarks {

    private final static Logger LOGGER = Logger.getLogger(SnapshotBenchmarks.class.getName());

    private static int ITERATIONS = 10;

    public static void main(String[] args) throws Exception {
        LOGGER.setLevel(Level.INFO);
        VariableSizedArraysBenchmark.benchmarkVariableSizedArrays();
        EventSelectionBenchmark.benchmarkEventSelection();
    }

    static abstract class Benchmark {
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


        static int[] getStateSizes(BProgram program, int num_steps) throws Exception {
            LOGGER.info("Measuring state size");
            ExecutorService execSvc = ExecutorServiceMaker.makeWithName("SnapshotStore");
            DfsBProgramVerifier sut = new DfsBProgramVerifier();
            BProgramSyncSnapshotIO io = new BProgramSyncSnapshotIO(program);
            ArrayList<Integer> snapshotSizes = new ArrayList<>();

            Node next = Node.getInitialNode(program, execSvc);

            //Iteration 1,starts already at request state A
            for (int i = 0; i < num_steps; i++) {
                snapshotSizes.add(io.serialize(next.getSystemState()).length);
                next = sut.getUnvisitedNextNode(next, execSvc);
            }
            execSvc.shutdown();

            return snapshotSizes.stream().mapToInt(i -> i).toArray();
        }

        static VerificationResult getVerification(DfsBProgramVerifier vfr, BProgram prog) throws Exception {
            return vfr.verify(prog);
        }

        static VerificationResult[] getVerification(DfsBProgramVerifier vfr, String programPath, Map<String, Object> valueMap, int iteration_count) {
            LOGGER.info("Measuring verification time");
            return LongStream.range(0, iteration_count).mapToObj(i -> {
                try {
                    BProgram prog = makeBProgram(programPath, valueMap);
                    return getVerification(vfr, prog);
                } catch (Exception ex) {
                    return new VerificationResult(VerificationResult.ViolationType.None, null, null);
                }
            }).toArray(VerificationResult[]::new);

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
            LOGGER.info("Measuring effect of integer size");
            return measureProgram(0);
        }

        private static BenchmarkResult measureObjectSize() throws Exception {
            LOGGER.info("Measuring effect of object size");
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
            VerificationResult[][] verificationTimes = new VerificationResult[ITERATIONS][];
            VerificationResult[][] verificationTimesHash = new VerificationResult[ITERATIONS][];
            BProgram[] programs = new BProgram[ITERATIONS];
            for (int i = 0; i < ITERATIONS; i++) {
                String res = String.format("Measuring for array step of %d size\n", ARRAY_STEP + i);
                LOGGER.info(res);
                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("INITIAL_ARRAY_SIZE", INITIAL_ARRAY_SIZE);
                valueMap.put("ARRAY_STEP", ARRAY_STEP + i);
                valueMap.put("NUM_STEPS", NUM_STEPS);
                valueMap.put("OBJECT_TYPE", object_type);

                BProgram programToTest = makeBProgram(IMPLEMENTATION, valueMap);
                programs[i] = programToTest;
                //have to clone the object because BPrograms are not reusable
                System.gc();
                snapshotSet[i] = getStateSizes(makeBProgram(IMPLEMENTATION, valueMap), NUM_STEPS);
                System.gc();
                verifier.setVisitedNodeStore(store);
                verificationTimes[i] = getVerification(verifier, IMPLEMENTATION, valueMap, ITERATIONS);
                verifier.setVisitedNodeStore(storeHash);
                verificationTimesHash[i] = getVerification(verifier, IMPLEMENTATION, valueMap, ITERATIONS);
                System.gc();

            }

            return new BenchmarkResult(testName, programs, snapshotSet, verificationTimes, verificationTimesHash);
        }

    }

    @SuppressWarnings("SameParameterValue")
    static class EventSelectionBenchmark extends Benchmark {
        static String IMPLEMENTATION = "benchmarks/eventSelection.js";
        static String TEST_NAME = "eventSelection";
        static int INITIAL_ARRAY_SIZE = 1;
        static int ARRAY_STEP = 5; //Size of each step, how many objects are we adding
        static int NUM_STEPS = 10; // How many steps to take

        static int MAX_THREADS = 10;

        static void benchmarkEventSelection() throws Exception {
            BenchmarkResult simpleEventSelectionResults = measureProgram(new SimpleEventSelectionStrategy(), 1,false);
            BenchmarkResult orderedEventSelectionResults = measureProgram(new OrderedEventSelectionStrategy(), 1, false);
            //BenchmarkResult PrioritizedBSyncEventSelectionResults = measureProgram(new PrioritizedBSyncEventSelectionStrategy());
            //BenchmarkResult PrioritizedBThreadsEventSelectionResults = measureProgram(new PrioritizedBThreadsEventSelectionStrategy());

            outputBenchResults(simpleEventSelectionResults);
            outputBenchResults(orderedEventSelectionResults);

            //Start from 2 because 1 was implicitly tested by simpleEvent
            for (int i = 1; i < MAX_THREADS; i++) {
                BenchmarkResult threadResults = measureProgram(new SimpleEventSelectionStrategy(), i, false);
                outputBenchResults(threadResults);
                BenchmarkResult threadResultsNoDisplay = measureProgram(new SimpleEventSelectionStrategy(), i, true);
                outputBenchResults(threadResultsNoDisplay);
            }
        }


        private static BenchmarkResult measureProgram(EventSelectionStrategy strategy, int num_threads, boolean save_sync) throws Exception {
            VisitedStateStore store = new BThreadSnapshotVisitedStateStore();
            VisitedStateStore storeHash = new HashVisitedStateStore();
            DfsBProgramVerifier verifier = new DfsBProgramVerifier();
            /*
                Test for variable num_steps
                we want to see if there's a non linear increase in snapshot size
             */
            int[][] snapshotSet = new int[ITERATIONS][];
            VerificationResult[][] verificationTimes = new VerificationResult[ITERATIONS][];
            VerificationResult[][] verificationTimesHash = new VerificationResult[ITERATIONS][];
            BProgram[] programs = new BProgram[ITERATIONS];


            for (int i = 0; i < ITERATIONS; i++) {

                String res = String.format("Measuring for array step of %d size\n", ARRAY_STEP + i);
                LOGGER.info(res);

                Map<String, Object> valueMap = new HashMap<>();
                valueMap.put("NUM_THREADS", num_threads);
                valueMap.put("INITIAL_ARRAY_SIZE", INITIAL_ARRAY_SIZE);
                valueMap.put("ARRAY_STEP", ARRAY_STEP + i);
                valueMap.put("NUM_STEPS", NUM_STEPS);
                valueMap.put("SAVE_EVENT", save_sync);


                //Initial copy
                BProgram programToTest = makeBProgram(IMPLEMENTATION, valueMap, strategy);
                programs[i] = programToTest;
                //Cleanup GC before and after, opportunistic
                //have to clone the program because BPrograms are not reusable
                System.gc();
                snapshotSet[i] = getStateSizes(makeBProgram(IMPLEMENTATION, valueMap, strategy), NUM_STEPS);
                System.gc();
                verifier.setVisitedNodeStore(store);
                verificationTimes[i] = getVerificationTime(verifier, IMPLEMENTATION, valueMap, ITERATIONS, strategy);
                verifier.setVisitedNodeStore(storeHash);
                verificationTimesHash[i] = getVerificationTime(verifier, IMPLEMENTATION, valueMap, ITERATIONS, strategy);
                System.gc();

            }

            return new BenchmarkResult(TEST_NAME + strategy.getClass().getName()
                    + "_save_state_" + Boolean.toString(save_sync) +
                    "_threads_" + Integer.toString(num_threads),
                    programs, snapshotSet, verificationTimes, verificationTimesHash);
        }


        private static BProgram makeBProgram(String programPath, Map<String, Object> value_map, EventSelectionStrategy strategy) {
            // prepare b-program
            BProgram bprog = makeBProgram(programPath, value_map);
            bprog.setEventSelectionStrategy(strategy);
            return bprog;
        }

        static VerificationResult[] getVerificationTime(DfsBProgramVerifier vfr, String programPath, Map<String, Object> valueMap, int iteration_count, EventSelectionStrategy strategy) {
            String res = String.format("Measuring Verification time with %s store", vfr.getVisitedNodeStore().getClass().getName());
            LOGGER.info(res);
            return LongStream.range(0, iteration_count).mapToObj(i -> {
                try {
                    BProgram prog = makeBProgram(programPath, valueMap,strategy);
                    return getVerification(vfr, prog);
                } catch (Exception ex) {
                    return new VerificationResult(VerificationResult.ViolationType.None, null, null);
                }
            }).toArray(VerificationResult[]::new);
        }
    }

    /**
     * This consolidates benchmark results, not generic enough yet
     */
    @SuppressWarnings("SameParameterValue")
    static class BenchmarkResult {
        final String benchName;
        final BProgram[] programs;
        final int[][] snapshotSizes;
        final VerificationResult[][] verificationTimesNoHash;
        final VerificationResult[][] verificationTimesHash;

        final ArrayList<String> verificationHeaders = new ArrayList<>(Collections.singletonList("Iteration count"));
        final ArrayList<String> snapshot_headers = new ArrayList<>(Collections.singletonList("Run instance"));

        BenchmarkResult(String name, BProgram[] programs, int[][] snapshotSizes, VerificationResult[][] verificationTimesNoHash, VerificationResult[][] verificationTimesHash) {
            this.benchName = name;
            this.programs = programs;
            this.snapshotSizes = snapshotSizes;
            this.verificationTimesNoHash = verificationTimesNoHash;
            this.verificationTimesHash = verificationTimesHash;
            //assume they're all identical, they better be
            IntStream.range(0, this.snapshotSizes[0].length).forEach(i -> snapshot_headers.add("raw " + i));
            IntStream.range(0, this.verificationTimesNoHash[0].length).forEach(i -> {
                verificationHeaders.add("time " + i);
                verificationHeaders.add("nodeCount " + i);
            });
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
                LongSummaryStatistics stats = Arrays.stream(verificationTimesNoHash[i]).mapToLong(VerificationResult::getTimeMillies).summaryStatistics();
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

        void outputTimesToCsv(Path filePath, VerificationResult[][] verificationResults) throws IOException {
            BufferedWriter out = Files.newBufferedWriter(filePath);
            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                    .withHeader(verificationHeaders.toArray(new String[0])))) {
                for (int i = 0; i < verificationResults.length; i++) {
                    Object[] toPrint = new Object[(verificationResults[i].length * 2) + 1];
                    toPrint[0] = i;
                    for (int j = 0; j < verificationResults[i].length; j++) {
                        int realIndex = (j * 2) + 1;
                        toPrint[realIndex] = verificationResults[i][j].getTimeMillies();
                        toPrint[realIndex + 1] = verificationResults[i][j].getScannedStatesCount();
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