package sorting;

import java.util.ArrayList;
import java.util.List;

public final class CountingSort {

    private CountingSort() {
        // Utility class.
    }

    public static List<Integer> sort(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        int maxValue = 0;
        for (int value : values) {
            if (value < 0) {
                throw new IllegalArgumentException("CountingSort only accepts non-negative integers.");
            }
            if (value > maxValue) {
                maxValue = value;
            }
        }

        int[] counts = new int[maxValue + 1];
        for (int value : values) {
            counts[value]++;
        }

        List<Integer> sortedValues = new ArrayList<>(values.size());
        for (int value = 0; value < counts.length; value++) {
            for (int i = 0; i < counts[value]; i++) {
                sortedValues.add(value);
            }
        }

        return sortedValues;
    }
}
