package it.polimi.algorithms.alns;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class BalancedPMedianALNS {

    private int n;
    private int p;
    private float[][] d;
    private float alpha;

    public BalancedPMedianALNS(final int n, final int p, final float[][] d, float alpha) {
        this.n = n;
        this.p = p;
        this.d = d;
        this.alpha = alpha;
    }

    public void run(Solution initialSolution) {
        while (!stoppingCondition()) {

        }
    }

    public boolean stoppingCondition() {
        return true;
    }

    public static class Solution {
        private int[] medians;
        private double value;

        public Solution(int[] medians, float[][] d, float alpha) {
            assert medians.length == d.length;
            this.medians = medians;
            this.value = computeValue(medians, d, alpha);
        }

        public int[] getMedians() {
            return medians;
        }

        public double getValue() {
            return value;
        }

        private double computeValue(int[] meds, float[][] d, float alpha) {
            double w = 0;
            Map<Integer, Integer> counts = new HashMap<>();
            for (int i=0; i<meds.length; i++) {
                w += d[i][meds[i]];
                int c = counts.getOrDefault(meds[i], 0);
                counts.put(meds[i], c+1);
            }

            int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
            for (int c : counts.values()) {
                if (c > max) max = c;
                if (c < min) min = c;
            }

            return w + alpha * (max - min) * (max - min);
        }
    }
}
