package sorting;

import java.util.ArrayList;
import java.util.List;

public final class QuickSort {

    private QuickSort() {
        // Utility class.
    }

    public static List<Integer> sort(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return quickSort(values);
    }

    private static List<Integer> quickSort(List<Integer> values) {
        if (values.size() <= 1) {
            return new ArrayList<>(values);
        }

        int pivot = values.get(values.size() / 2);
        List<Integer> left = new ArrayList<>();
        List<Integer> middle = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        for (int value : values) {
            if (value < pivot) {
                left.add(value);
            } else if (value > pivot) {
                right.add(value);
            } else {
                middle.add(value);
            }
        }

        List<Integer> result = new ArrayList<>(values.size());
        result.addAll(quickSort(left));
        result.addAll(middle);
        result.addAll(quickSort(right));
        return result;
    }
}
