package sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ARUCountingSort {

    private ARUCountingSort() {
        // Utility class.
    }

    public static List<Integer> sort(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        int maxValue = 0;
        for (int value : values) {
            if (value < 0) {
                throw new IllegalArgumentException("ARUCountingSort only accepts non-negative integers.");
            }
            if (value > maxValue) {
                maxValue = value;
            }
        }

        DebugState debugState = sortInternal(values, maxValue, false);
        List<Integer> sortedValues = new ArrayList<>(values.size());
        for (int value : debugState.sorted) {
            sortedValues.add(value);
        }
        return sortedValues;
    }

    public static DebugState sortWithDebug(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return new DebugState(1, new int[0], new int[0], new int[0], new int[0]);
        }

        int maxValue = 0;
        for (int value : values) {
            if (value < 0) {
                throw new IllegalArgumentException("ARUCountingSort only accepts non-negative integers.");
            }
            if (value > maxValue) {
                maxValue = value;
            }
        }
        return sortInternal(values, maxValue, true);
    }

    private static DebugState sortInternal(List<Integer> values, int maxValue, boolean keepDebugArrays) {
        // m is chosen as ceil(sqrt(k + 1)) to keep both quotient and remainder in [0, m - 1].
        // This preserves O(n + sqrt(k)) while avoiding quotient overflow on non-square k.
        int m = (int) Math.ceil(Math.sqrt((double) maxValue + 1.0));
        if (m < 1) {
            m = 1;
        }

        int[] input = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            input[i] = values.get(i);
        }

        int[] qCounts = new int[m];
        int[] rCounts = new int[m];

        for (int value : input) {
            qCounts[value / m]++;
            rCounts[value % m]++;
        }

        int[] qPrefix = Arrays.copyOf(qCounts, qCounts.length);
        int[] rPrefix = Arrays.copyOf(rCounts, rCounts.length);
        prefixSumInPlace(qPrefix);
        prefixSumInPlace(rPrefix);

        int[] byRemainder = stableCountPassByRemainder(input, m, rPrefix);
        int[] sorted = stableCountPassByQuotient(byRemainder, m, qPrefix);

        return new DebugState(
                m,
                keepDebugArrays ? qPrefix : new int[0],
                keepDebugArrays ? rPrefix : new int[0],
                keepDebugArrays ? byRemainder : new int[0],
                sorted
        );
    }

    private static void prefixSumInPlace(int[] values) {
        for (int i = 1; i < values.length; i++) {
            values[i] += values[i - 1];
        }
    }

    private static int[] stableCountPassByRemainder(int[] input, int m, int[] prefix) {
        int[] output = new int[input.length];
        int[] positions = Arrays.copyOf(prefix, prefix.length);
        for (int i = input.length - 1; i >= 0; i--) {
            int value = input[i];
            int key = value % m;
            int pos = --positions[key];
            output[pos] = value;
        }
        return output;
    }

    private static int[] stableCountPassByQuotient(int[] input, int m, int[] prefix) {
        int[] output = new int[input.length];
        int[] positions = Arrays.copyOf(prefix, prefix.length);
        for (int i = input.length - 1; i >= 0; i--) {
            int value = input[i];
            int key = value / m;
            int pos = --positions[key];
            output[pos] = value;
        }
        return output;
    }

    public static final class DebugState {
        private final int m;
        private final int[] qPrefix;
        private final int[] rPrefix;
        private final int[] byRemainder;
        private final int[] sorted;

        private DebugState(int m, int[] qPrefix, int[] rPrefix, int[] byRemainder, int[] sorted) {
            this.m = m;
            this.qPrefix = qPrefix;
            this.rPrefix = rPrefix;
            this.byRemainder = byRemainder;
            this.sorted = sorted;
        }

        public int getM() {
            return m;
        }

        public int[] getQPrefix() {
            return Arrays.copyOf(qPrefix, qPrefix.length);
        }

        public int[] getRPrefix() {
            return Arrays.copyOf(rPrefix, rPrefix.length);
        }

        public int[] getByRemainder() {
            return Arrays.copyOf(byRemainder, byRemainder.length);
        }

        public int[] getSorted() {
            return Arrays.copyOf(sorted, sorted.length);
        }
    }
}
