package sorting;

import java.util.ArrayList;
import java.util.List;

public final class MergeSort {

    private MergeSort() {
        // Utility class.
    }

    public static List<Integer> sort(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        return mergeSort(new ArrayList<>(values));
    }

    private static List<Integer> mergeSort(List<Integer> values) {
        if (values.size() <= 1) {
            return values;
        }

        int mid = values.size() / 2;
        List<Integer> left = mergeSort(new ArrayList<>(values.subList(0, mid)));
        List<Integer> right = mergeSort(new ArrayList<>(values.subList(mid, values.size())));
        return merge(left, right);
    }

    private static List<Integer> merge(List<Integer> left, List<Integer> right) {
        List<Integer> merged = new ArrayList<>(left.size() + right.size());
        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {
            if (left.get(i) <= right.get(j)) {
                merged.add(left.get(i));
                i++;
            } else {
                merged.add(right.get(j));
                j++;
            }
        }

        while (i < left.size()) {
            merged.add(left.get(i));
            i++;
        }

        while (j < right.size()) {
            merged.add(right.get(j));
            j++;
        }

        return merged;
    }
}
