package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DataGenerator {

    private DataGenerator() {
        // Utility class.
    }

    public static int[] randomArray(int size, int maxValueInclusive, Random random) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = random.nextInt(maxValueInclusive + 1);
        }
        return values;
    }

    public static List<Integer> toList(int[] values) {
        List<Integer> list = new ArrayList<>(values.length);
        for (int value : values) {
            list.add(value);
        }
        return list;
    }
}
