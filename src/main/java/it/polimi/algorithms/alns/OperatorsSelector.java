package it.polimi.algorithms.alns;

import it.polimi.domain.Solution;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OperatorsSelector {

    private class Selector {
        private final double[] probs;
        private final double[] success;
        private final int[] counters;

        public Selector(int size) {
            this.probs = new double[size];
            this.success = new double[size];
            this.counters = new int[size];
            Arrays.fill(probs, 1./size);
        }

        public int select() {
            double sampled = random.nextDouble();
            double s = 0.;
            if (sampled == 0){
                counters[0] += 1;
                return 0;
            }
            for (int i=0; i<probs.length; i++) {
                if (s < sampled && sampled <= s + probs[i]) {
                    counters[i] += 1;
                    return i;
                }
                s += probs[i];
            }
            throw new IllegalStateException("Error, no valid index found. Maybe the probabilities do not add up to 1.");
        }

        public void update(int idx, Solution accepted, Solution current, Solution best) {
            if (accepted.getCost() < best.getCost()) {
                success[idx] += d1;
            } else if (accepted.getCost() < current.getCost()) {
                success[idx] += d2;
            } else {
                success[idx] += d3;
            }
        }

        public void updateWeights() {
            double sum = 0.;
            for (int i=0; i<probs.length; i++) {
                probs[i] = probs[i] * (1 - r);
                if (counters[i] != 0)
                    probs[i] += r * success[i] / counters[i];
                sum += probs[i];
            }
            for (int i=0; i<probs.length; i++)
                probs[i] /= sum;
            Arrays.fill(counters, 0);
            Arrays.fill(success, 0);
        }
    }

    private final Random random;
    private final double d1;
    private final double d2;
    private final double d3;
    private final double r;

    private Selector ruinSelector;
    private Selector recreateSelector;

    public OperatorsSelector(final double d1, final double d2, final double d3, final double r) {
        this(d1, d2, d3, r, new Random());
    }

    public OperatorsSelector(final double d1, final double d2, final double d3, final double r, final int seed) {
        this(d1, d2, d3, r, new Random(seed));
    }

    public OperatorsSelector(final double d1, final double d2, final double d3, final double r, final Random random) {
        this.random = random;
        this.d1 = d1;
        this.d2 = d2;
        this.d3 = d3;
        this.r = r;
    }

    public void setOperatorSizes(final int ruinOperatorSize, final int recreateOperatorSize) {
        this.ruinSelector = new Selector(ruinOperatorSize);
        this.recreateSelector = new Selector(recreateOperatorSize);
    }

    public int selectRuinOperatorIndex() {
        return ruinSelector.select();
    }

    public int selectRecreateOperatorIndex() {
        return recreateSelector.select();
    }

    public void update(int ruinOperatorIdx, int recreateOperatorIdx, Solution accepted, Solution current, Solution best) {
        this.ruinSelector.update(ruinOperatorIdx, accepted, current, best);
        this.recreateSelector.update(recreateOperatorIdx, accepted, current, best);
    }

    public void updateWeights() {
        this.ruinSelector.updateWeights();
        this.recreateSelector.updateWeights();
    }
}
