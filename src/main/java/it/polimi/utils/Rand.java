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

    public static int sampleIndex(final double[] probabilities, final Random random) {
        double sampled = random.nextDouble();
        double s = 0.;
        if (sampled == 0) return 0;
        for (int i=0; i<probabilities.length; i++) {
            if (s < sampled && sampled <= s + probabilities[i])
                return i;
            s += probabilities[i];
        }
        throw new IllegalStateException("Error, no valid index found. Maybe the probabilities do not add up to 1.");
    }

    public static <T> T sample(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }

    public static <T> T sampleAndRemove(List<T> list, Random random) {
        return list.remove(random.nextInt(list.size()));
    }
}
