package algorithms;

import java.util.Arrays;
import java.util.List;

import sorting.ARUCountingSort;
import sorting.CountingSort;
import sorting.QuickSort;

public final class Demo {

    private Demo() {
        // Entry-point holder class.
    }

    public static void main(String[] args) {
        List<Integer> nonNegativeSample = Arrays.asList(4, 2, 2, 8, 3, 3, 1);
        List<Integer> mixedSample = Arrays.asList(7, -3, 4, -1, 0, -3, 2);
        List<Integer> quickSample = Arrays.asList(10, 7, 8, 9, 1, 5);

        System.out.println("Counting Sort: " + CountingSort.sort(nonNegativeSample));
        System.out.println("ARU Counting Sort: " + ARUCountingSort.sort(mixedSample));
        System.out.println("Quick Sort: " + QuickSort.sort(quickSample));
    }
}
