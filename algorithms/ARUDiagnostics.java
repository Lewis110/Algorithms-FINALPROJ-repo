package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import sorting.ARUCountingSort;
import sorting.CountingSort;

public final class ARUDiagnostics {
    private static final int N = 10_000;
    private static final int[] K_VALUES = {1_000, 10_000, 100_000, 1_000_000};
    private static final int WARMUP_RUNS = 10;
    private static final int MEASURED_RUNS = 10;
    private static final int MIN_REPETITIONS_PER_TRIAL = 5;
    private static final int MAX_REPETITIONS_PER_TRIAL = 50;
    private static final double TARGET_TRIAL_TIME_MS = 200.0;
    private static final long RANDOM_SEED = 99L;

    private ARUDiagnostics() {
        // Utility class.
    }

    public static void main(String[] args) {
        runCorrectnessDiagnostic();
        runPerformanceSanityDiagnostic();
    }

    private static void runCorrectnessDiagnostic() {
        int[] sample = {17, 2, 38, 63, 19, 44, 29, 11};
        List<Integer> input = DataGenerator.toList(sample);

        ARUCountingSort.DebugState debug = ARUCountingSort.sortWithDebug(input);
        List<Integer> output = ARUCountingSort.sort(new ArrayList<>(input));
        List<Integer> expected = Arrays.asList(2, 11, 17, 19, 29, 38, 44, 63);

        System.out.println("=== ARU Correctness Diagnostic ===");
        System.out.println("Input: " + Arrays.toString(sample));
        System.out.println("m: " + debug.getM());
        System.out.println("Q prefix: " + Arrays.toString(debug.getQPrefix()));
        System.out.println("R prefix: " + Arrays.toString(debug.getRPrefix()));
        System.out.println("B after remainder pass: " + Arrays.toString(debug.getByRemainder()));
        System.out.println("Output: " + output);
        System.out.println("Expected: " + expected);
        System.out.println("Matches expected: " + expected.equals(output));
        System.out.println();
    }

    private static void runPerformanceSanityDiagnostic() {
        System.out.println("=== ARU Performance Sanity (n=10000) ===");
        System.out.println("Algorithm,k,time_ms_avg");

        Random random = new Random(RANDOM_SEED);
        for (int k : K_VALUES) {
            int[] baseInput = DataGenerator.randomArray(N, k, random);

            Metrics.Measurement countMeasurement = Metrics.averageMeasurement(
                    CountingSort::sort,
                    baseInput,
                    WARMUP_RUNS,
                    MEASURED_RUNS,
                    MIN_REPETITIONS_PER_TRIAL,
                    MAX_REPETITIONS_PER_TRIAL,
                    TARGET_TRIAL_TIME_MS
            );

            Metrics.Measurement aruMeasurement = Metrics.averageMeasurement(
                    ARUCountingSort::sort,
                    baseInput,
                    WARMUP_RUNS,
                    MEASURED_RUNS,
                    MIN_REPETITIONS_PER_TRIAL,
                    MAX_REPETITIONS_PER_TRIAL,
                    TARGET_TRIAL_TIME_MS
            );

            System.out.println("Count Sort," + k + "," + String.format("%.6f", countMeasurement.getAverageTimeMs()));
            System.out.println("ARU Count Sort," + k + "," + String.format("%.6f", aruMeasurement.getAverageTimeMs()));
            System.out.println("Repetitions used: Count Sort=" + countMeasurement.getRepetitionsPerTrial()
                    + ", ARU Count Sort=" + aruMeasurement.getRepetitionsPerTrial());

            if (aruMeasurement.getAverageTimeMs() > countMeasurement.getAverageTimeMs() * 1.20) {
                System.out.println("WARNING: ARU is behaving slower than expected at k=" + k);
            }
        }
    }
}
