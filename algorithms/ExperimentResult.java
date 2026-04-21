package algorithms;

public final class ExperimentResult {
    private final String algorithm;
    private final int n;
    private final int k;
    private final double averageTimeMs;
    private final double standardDeviationMs;
    private final double averageMemoryBytes;
    private final int repetitionsPerTrial;

    public ExperimentResult(
            String algorithm,
            int n,
            int k,
            double averageTimeMs,
            double standardDeviationMs,
            double averageMemoryBytes,
            int repetitionsPerTrial
    ) {
        this.algorithm = algorithm;
        this.n = n;
        this.k = k;
        this.averageTimeMs = averageTimeMs;
        this.standardDeviationMs = standardDeviationMs;
        this.averageMemoryBytes = averageMemoryBytes;
        this.repetitionsPerTrial = repetitionsPerTrial;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getN() {
        return n;
    }

    public int getK() {
        return k;
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
