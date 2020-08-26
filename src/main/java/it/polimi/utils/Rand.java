package it.polimi.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Rand {

    public static int[] permutateRange(final int fromInclusive, final int toExclusive, final Random random) {
        List<Integer> ls = IntStream.range(fromInclusive, toExclusive).boxed().collect(Collectors.toList());
        Collections.shuffle(ls, random);
        return ls.stream().mapToInt(i -> i).toArray();
    }

    public static int[] permutateRange(final int fromInclusive, final int toExclusive, final int seed) {
        return permutateRange(fromInclusive, toExclusive, new Random(seed));
    }

    public static int[] permutateRange(final int fromInclusive, final int toExclusive) {
        return permutateRange(fromInclusive, toExclusive, new Random());
    }

}
