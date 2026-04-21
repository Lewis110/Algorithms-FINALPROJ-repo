package algorithms;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import sorting.ARUCountingSort;
import sorting.CountingSort;
import sorting.MergeSort;
import sorting.QuickSort;

public final class ExperimentRunner {
    private static final int[] N_VALUES = {100_000, 500_000};
    private static final int[] K_VALUES = {1_000, 10_000, 100_000, 1_000_000, 10_000_000};

    private static final int WARMUP_RUNS = 10;
    private static final int MEASURED_RUNS = 10;
    private static final int MIN_REPETITIONS_PER_TRIAL = 5;
    private static final int MAX_REPETITIONS_PER_TRIAL = 50;
    private static final double TARGET_TRIAL_TIME_MS = 200.0;
    private static final long RANDOM_SEED = 42L;

    private static final String ALGORITHM_COUNT_SORT = "Count Sort";
    private static final String ALGORITHM_ARU_COUNT_SORT = "ARU Count Sort";
    private static final String ALGORITHM_QUICK_SORT = "Quick Sort";
    private static final String ALGORITHM_MERGE_SORT = "Merge Sort";

    private static final Path OUTPUT_DIR = Paths.get("output");

    private ExperimentRunner() {
        // Utility class.
    }

    public static void main(String[] args) throws IOException {
        Random random = new Random(RANDOM_SEED);

        List<ExperimentResult> timeVsKResults = new ArrayList<>();
        List<ExperimentResult> spaceVsKResults = new ArrayList<>();
        List<ExperimentResult> comparisonAllAlgorithms = new ArrayList<>();

        for (int n : N_VALUES) {
            for (int k : K_VALUES) {
                int[] baseInput = DataGenerator.randomArray(n, k, random);
                validateAlgorithmOutputs(baseInput);

                runAndStore(ALGORITHM_COUNT_SORT, baseInput, n, k, timeVsKResults, spaceVsKResults, comparisonAllAlgorithms);
                runAndStore(ALGORITHM_ARU_COUNT_SORT, baseInput, n, k, timeVsKResults, spaceVsKResults, comparisonAllAlgorithms);
                runAndStore(ALGORITHM_QUICK_SORT, baseInput, n, k, null, null, comparisonAllAlgorithms);
                runAndStore(ALGORITHM_MERGE_SORT, baseInput, n, k, null, null, comparisonAllAlgorithms);
            }
        }

        CSVWriter.writeResults(OUTPUT_DIR.resolve("time_vs_k.csv"), timeVsKResults);
        CSVWriter.writeResults(OUTPUT_DIR.resolve("space_vs_k.csv"), spaceVsKResults);
        CSVWriter.writeResults(OUTPUT_DIR.resolve("comparison_all_algorithms.csv"), comparisonAllAlgorithms);

        System.out.println("Experiments complete.");
        System.out.println("Generated CSV files:");
        System.out.println("- " + OUTPUT_DIR.resolve("time_vs_k.csv"));
        System.out.println("- " + OUTPUT_DIR.resolve("space_vs_k.csv"));
        System.out.println("- " + OUTPUT_DIR.resolve("comparison_all_algorithms.csv"));
        System.out.println("Sorts used: "
                + ALGORITHM_COUNT_SORT + ", "
                + ALGORITHM_ARU_COUNT_SORT + ", "
                + ALGORITHM_QUICK_SORT + ", "
                + ALGORITHM_MERGE_SORT);
        System.out.println("Validation: each algorithm output matches Java baseline sort.");
        printTrendDiagnostics(comparisonAllAlgorithms);
    }

    private static void runAndStore(
            String algorithmName,
            int[] baseInput,
            int n,
            int k,
            List<ExperimentResult> timeVsKResults,
            List<ExperimentResult> spaceVsKResults,
            List<ExperimentResult> comparisonResults
    ) {
        Metrics.Measurement measurement = measureAlgorithm(algorithmName, baseInput);
        long analyticalSpace = computeAnalyticalSpaceBytes(algorithmName, n, k);
        double memoryBytes = analyticalSpace >= 0
                ? (double) analyticalSpace
                : measurement.getAverageMemoryBytes();
        ExperimentResult result = new ExperimentResult(
                algorithmName,
                n,
                k,
                measurement.getAverageTimeMs(),
                measurement.getStandardDeviationMs(),
                memoryBytes,
                measurement.getRepetitionsPerTrial()
        );

        comparisonResults.add(result);
        if (ALGORITHM_COUNT_SORT.equals(algorithmName) || ALGORITHM_ARU_COUNT_SORT.equals(algorithmName)) {
            if (timeVsKResults != null) {
                timeVsKResults.add(result);
            }
            if (spaceVsKResults != null) {
                spaceVsKResults.add(result);
            }
        }
    }

    private static long computeAnalyticalSpaceBytes(String algorithmName, int n, int k) {
        switch (algorithmName) {
            case ALGORITHM_COUNT_SORT:
                // int[k+1] counts array + output ArrayList backing Object[n]
                return 4L * (k + 1) + 4L * n;
            case ALGORITHM_ARU_COUNT_SORT:
                int m = (int) Math.ceil(Math.sqrt((double) k + 1.0));
                if (m < 1) {
                    m = 1;
                }
                // Peak during sortInternal (quotient pass):
                //   int[n] input + int[n] byRemainder + int[n] output = 3 * 4n
                //   int[m] x6 (qCounts, rCounts, qPrefix, rPrefix, positions) = 6 * 4m
                // Plus output ArrayList backing Object[n] = 4n
                return 16L * n + 24L * m;
            default:
                return -1L;
        }
    }

    private static Metrics.Measurement measureAlgorithm(String algorithmName, int[] baseInput) {
        switch (algorithmName) {
            case ALGORITHM_COUNT_SORT:
                return Metrics.averageMeasurement(
                        CountingSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
            case ALGORITHM_ARU_COUNT_SORT:
                return Metrics.averageMeasurement(
                        ARUCountingSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
            case ALGORITHM_QUICK_SORT:
                return Metrics.averageMeasurement(
                        QuickSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
            case ALGORITHM_MERGE_SORT:
                return Metrics.averageMeasurement(
                        MergeSort::sort,
                        baseInput,
                        WARMUP_RUNS,
                        MEASURED_RUNS,
                        MIN_REPETITIONS_PER_TRIAL,
                        MAX_REPETITIONS_PER_TRIAL,
                        TARGET_TRIAL_TIME_MS
                );
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithmName);
        }
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

    private static void printTrendDiagnostics(List<ExperimentResult> results) {
        System.out.println("Diagnostics:");
        for (int n : N_VALUES) {
            List<ExperimentResult> byN = results.stream()
                    .filter(result -> result.getN() == n)
                    .sorted(Comparator.comparingInt(ExperimentResult::getK))
                    .collect(Collectors.toList());

            Map<String, List<ExperimentResult>> byAlgorithm = byN.stream()
                    .collect(Collectors.groupingBy(ExperimentResult::getAlgorithm));

            List<ExperimentResult> count = byAlgorithm.getOrDefault(ALGORITHM_COUNT_SORT, List.of());
            List<ExperimentResult> aru = byAlgorithm.getOrDefault(ALGORITHM_ARU_COUNT_SORT, List.of());
            List<ExperimentResult> quick = byAlgorithm.getOrDefault(ALGORITHM_QUICK_SORT, List.of());
            List<ExperimentResult> merge = byAlgorithm.getOrDefault(ALGORITHM_MERGE_SORT, List.of());

            if (!isIncreasing(count, 1.20)) {
                System.out.println("WARNING (n=" + n + "): Count Sort is not clearly increasing with k.");
            }
            if (!isLessOrEqualAtHighK(aru, count, 1.10)) {
                System.out.println("WARNING (n=" + n + "): ARU Count Sort does not outperform/track Count Sort at high k.");
            }
            if (!isStableAcrossK(quick, 1.80)) {
                System.out.println("WARNING (n=" + n + "): Quick Sort varies strongly with k.");
            }
            if (!isStableAcrossK(merge, 1.80)) {
                System.out.println("WARNING (n=" + n + "): Merge Sort varies strongly with k.");
            }
        }
    }

    private static boolean isIncreasing(List<ExperimentResult> data, double minGrowthRatio) {
        if (data.size() < 2) {
            return false;
        }
        double first = data.get(0).getAverageTimeMs();
        double last = data.get(data.size() - 1).getAverageTimeMs();
        return last >= first * minGrowthRatio;
    }

    private static boolean isLessOrEqualAtHighK(List<ExperimentResult> aru, List<ExperimentResult> count, double toleranceRatio) {
        if (aru.isEmpty() || count.isEmpty()) {
            return false;
        }
        double aruHighK = aru.get(aru.size() - 1).getAverageTimeMs();
        double countHighK = count.get(count.size() - 1).getAverageTimeMs();
        return aruHighK <= countHighK * toleranceRatio;
    }

    private static boolean isStableAcrossK(List<ExperimentResult> data, double allowedSpreadRatio) {
        if (data.isEmpty()) {
            return false;
        }
        double min = data.stream().mapToDouble(ExperimentResult::getAverageTimeMs).min().orElse(0.0);
        double max = data.stream().mapToDouble(ExperimentResult::getAverageTimeMs).max().orElse(0.0);
        if (min <= 0.0) {
            return false;
        }
        return (max / min) <= allowedSpreadRatio;
    }
}
