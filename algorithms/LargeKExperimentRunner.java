package algorithms;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import sorting.ARUCountingSort;
import sorting.CountingSort;
import sorting.MergeSort;
import sorting.QuickSort;

public final class LargeKExperimentRunner {
    private static final int N = 10_000;
    private static final int[] K_VALUES = {1_000, 10_000, 100_000, 1_000_000, 5_000_000, 10_000_000};

    private static final int WARMUP_RUNS = 10;
    private static final int MEASURED_RUNS = 10;
    private static final int MIN_REPETITIONS_PER_TRIAL = 5;
    private static final int MAX_REPETITIONS_PER_TRIAL = 50;
    private static final double TARGET_TRIAL_TIME_MS = 200.0;
    private static final long RANDOM_SEED = 84L;

    private static final String ALGORITHM_COUNT_SORT = "Count Sort";
    private static final String ALGORITHM_ARU_COUNT_SORT = "ARU Count Sort";
    private static final String ALGORITHM_QUICK_SORT = "Quick Sort";
    private static final String ALGORITHM_MERGE_SORT = "Merge Sort";

    private static final Path OUTPUT_DIR = Paths.get("output");

    private LargeKExperimentRunner() {
        // Utility class.
    }

    public static void main(String[] args) throws IOException {
        Random random = new Random(RANDOM_SEED);
        List<ExperimentResult> comparisonAllAlgorithms = new ArrayList<>();

        for (int k : K_VALUES) {
            int[] baseInput = DataGenerator.randomArray(N, k, random);
            validateAlgorithmOutputs(baseInput);

            comparisonAllAlgorithms.add(measure(ALGORITHM_COUNT_SORT, baseInput, N, k));
            comparisonAllAlgorithms.add(measure(ALGORITHM_ARU_COUNT_SORT, baseInput, N, k));
            comparisonAllAlgorithms.add(measure(ALGORITHM_QUICK_SORT, baseInput, N, k));
            comparisonAllAlgorithms.add(measure(ALGORITHM_MERGE_SORT, baseInput, N, k));
        }

        CSVWriter.writeResults(OUTPUT_DIR.resolve("comparison_all_algorithms_large_k.csv"), comparisonAllAlgorithms);

        System.out.println("Large-k experiment complete.");
        System.out.println("Generated CSV file:");
        System.out.println("- " + OUTPUT_DIR.resolve("comparison_all_algorithms_large_k.csv"));
        System.out.println("Sorts used: "
                + ALGORITHM_COUNT_SORT + ", "
                + ALGORITHM_ARU_COUNT_SORT + ", "
                + ALGORITHM_QUICK_SORT + ", "
                + ALGORITHM_MERGE_SORT);
    }

    private static long computeAnalyticalSpaceBytes(String algorithmName, int n, int k) {
        switch (algorithmName) {
            case ALGORITHM_COUNT_SORT:
                return 4L * (k + 1) + 4L * n;
            case ALGORITHM_ARU_COUNT_SORT:
                int m = (int) Math.ceil(Math.sqrt((double) k + 1.0));
                if (m < 1) {
                    m = 1;
                }
                return 16L * n + 24L * m;
            default:
                return -1L;
        }
    }

    private static ExperimentResult measure(String algorithmName, int[] baseInput, int n, int k) {
        Metrics.Measurement measurement;
        switch (algorithmName) {
            case ALGORITHM_COUNT_SORT:
                measurement = Metrics.averageMeasurement(
                        CountingSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
                break;
            case ALGORITHM_ARU_COUNT_SORT:
                measurement = Metrics.averageMeasurement(
                        ARUCountingSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
                break;
            case ALGORITHM_QUICK_SORT:
                measurement = Metrics.averageMeasurement(
                        QuickSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
                break;
            case ALGORITHM_MERGE_SORT:
                measurement = Metrics.averageMeasurement(
                        MergeSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
                break;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithmName);
        }

        long analyticalSpace = computeAnalyticalSpaceBytes(algorithmName, n, k);
        double memoryBytes = analyticalSpace >= 0
                ? (double) analyticalSpace
                : measurement.getAverageMemoryBytes();
        return new ExperimentResult(
                algorithmName,
                n,
                k,
                measurement.getAverageTimeMs(),
                measurement.getStandardDeviationMs(),
                memoryBytes,
                measurement.getRepetitionsPerTrial()
        );
    }

    private static void validateAlgorithmOutputs(int[] baseInput) {
        List<Integer> input = DataGenerator.toList(baseInput);
        List<Integer> expected = new ArrayList<>(input);
        Collections.sort(expected);

        assertSortedEquals(expected, CountingSort.sort(new ArrayList<>(input)), ALGORITHM_COUNT_SORT);
        assertSortedEquals(expected, ARUCountingSort.sort(new ArrayList<>(input)), ALGORITHM_ARU_COUNT_SORT);
        assertSortedEquals(expected, QuickSort.sort(new ArrayList<>(input)), ALGORITHM_QUICK_SORT);
        assertSortedEquals(expected, MergeSort.sort(new ArrayList<>(input)), ALGORITHM_MERGE_SORT);
    }

    private static void assertSortedEquals(List<Integer> expected, List<Integer> actual, String algorithmName) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("Incorrect output from " + algorithmName);
        }
    }
}
