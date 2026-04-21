package algorithms;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class CSVWriter {

    private CSVWriter() {
        // Utility class.
    }

    public static void writeResults(Path outputPath, List<ExperimentResult> results) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("algorithm,n,k,avg_time_ms,stddev_time_ms,avg_memory_bytes,repetitions_per_trial");
            writer.newLine();
            for (ExperimentResult result : results) {
                writer.write(
                        result.getAlgorithm()
                                + "," + result.getN()
                                + "," + result.getK()
                                + "," + String.format(Locale.US, "%.6f", result.getAverageTimeMs())
                                + "," + String.format(Locale.US, "%.6f", result.getStandardDeviationMs())
                                + "," + Math.round(result.getAverageMemoryBytes())
                                + "," + result.getRepetitionsPerTrial()
                );
                writer.newLine();
            }
        }
    }
}
