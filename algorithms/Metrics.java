package algorithms;

import java.util.ArrayList;
import java.util.List;

public final class Metrics {
    @SuppressWarnings("unused")
    private static volatile int blackhole;

    private Metrics() {
        // Utility class.
    }

    @FunctionalInterface
    public interface SortAlgorithm {
        List<Integer> sort(List<Integer> values);
    }

    public static Measurement averageMeasurement(
            SortAlgorithm algorithm,
            int[] source,
            int warmupRuns,
            int measuredRuns,
            int minRepetitionsPerTrial,
            int maxRepetitionsPerTrial,
            double targetTrialTimeMs
    ) {
        List<Integer> baseInput = DataGenerator.toList(source);

        for (int i = 0; i < warmupRuns; i++) {
            List<Integer> warmupInput = new ArrayList<>(baseInput);
            List<Integer> result = algorithm.sort(warmupInput);
            blackhole = result.size();
        }

        double calibrationMs = calibrateSingleSortMs(algorithm, baseInput);
        int repetitionsPerTrial = chooseRepetitions(calibrationMs, minRepetitionsPerTrial, maxRepetitionsPerTrial, targetTrialTimeMs);

        double[] perTrialTimeMs = new double[measuredRuns];
        long totalMemoryBytes = 0L;
        for (int i = 0; i < measuredRuns; i++) {
            long memoryBefore = usedMemoryBytes();
            long start = System.nanoTime();
            List<Integer> result = null;
            for (int j = 0; j < repetitionsPerTrial; j++) {
                List<Integer> runInput = new ArrayList<>(baseInput);
                result = algorithm.sort(runInput);
            }
            long elapsedNanos = System.nanoTime() - start;
            long memoryAfter = usedMemoryBytes();

            if (result != null) {
                blackhole = result.size();
            }

            perTrialTimeMs[i] = (elapsedNanos / 1_000_000.0) / repetitionsPerTrial;
            totalMemoryBytes += Math.max(0L, memoryAfter - memoryBefore) / repetitionsPerTrial;
        }

        double avgTimeMsPerSort = mean(perTrialTimeMs);
        double standardDeviationMs = standardDeviation(perTrialTimeMs, avgTimeMsPerSort);
        double avgMemoryBytesPerSort = ((double) totalMemoryBytes) / measuredRuns;

        return new Measurement(
                avgTimeMsPerSort,
                standardDeviationMs,
                avgMemoryBytesPerSort,
                repetitionsPerTrial
        );
    }

    private static double calibrateSingleSortMs(SortAlgorithm algorithm, List<Integer> baseInput) {
        long start = System.nanoTime();
        List<Integer> calibrationInput = new ArrayList<>(baseInput);
        List<Integer> result = algorithm.sort(calibrationInput);
        blackhole = result.size();
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private static int chooseRepetitions(
            double calibrationMs,
            int minRepetitionsPerTrial,
            int maxRepetitionsPerTrial,
            double targetTrialTimeMs
    ) {
        if (calibrationMs <= 0.0) {
            return maxRepetitionsPerTrial;
        }

        int suggested = (int) Math.ceil(targetTrialTimeMs / calibrationMs);
        if (suggested < minRepetitionsPerTrial) {
            return minRepetitionsPerTrial;
        }
        if (suggested > maxRepetitionsPerTrial) {
            return maxRepetitionsPerTrial;
        }
        return suggested;
    }

    private static double mean(double[] values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total / values.length;
    }

    private static double standardDeviation(double[] values, double mean) {
        if (values.length <= 1) {
            return 0.0;
        }

        double squaredDiffSum = 0.0;
        for (double value : values) {
            double diff = value - mean;
            squaredDiffSum += diff * diff;
        }
        return Math.sqrt(squaredDiffSum / values.length);
    }

    private static long usedMemoryBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static final class Measurement {
        private final double averageTimeMs;
        private final double standardDeviationMs;
        private final double averageMemoryBytes;
        private final int repetitionsPerTrial;

        public Measurement(double averageTimeMs, double standardDeviationMs, double averageMemoryBytes, int repetitionsPerTrial) {
            this.averageTimeMs = averageTimeMs;
            this.standardDeviationMs = standardDeviationMs;
            this.averageMemoryBytes = averageMemoryBytes;
            this.repetitionsPerTrial = repetitionsPerTrial;
        }

        public double getAverageTimeMs() {
            return averageTimeMs;
        }

        public double getStandardDeviationMs() {
            return standardDeviationMs;
        }

        public double getAverageMemoryBytes() {
            return averageMemoryBytes;
        }

        public int getRepetitionsPerTrial() {
            return repetitionsPerTrial;
        }
    }
}
