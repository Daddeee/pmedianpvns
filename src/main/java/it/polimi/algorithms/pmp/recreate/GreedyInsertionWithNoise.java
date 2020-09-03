package it.polimi.algorithms.pmp.recreate;


import it.polimi.domain.Location;
import it.polimi.algorithms.pmp.domain.PMPSolution;
import it.polimi.algorithms.pmp.domain.PMProblem;

import java.util.Random;

public class GreedyInsertionWithNoise extends GreedyInsertion {
    private PMProblem pmp;
    private double maxN;
    private Random random;

    public GreedyInsertionWithNoise(PMProblem pmp, double maxN) {
        super(pmp);
        this.maxN = maxN;
        this.random = new Random();
    }

    public GreedyInsertionWithNoise(PMProblem pmp, double maxN, int seed) {
        super(pmp);
        this.maxN = maxN;
        this.random = new Random(seed);
    }

    @Override
    protected double getInsertionDelta(PMPSolution pmps, Location location) {
        return Math.max(0, super.getInsertionDelta(pmps, location) + getNoise());
    }

    private double getNoise() {
        return -maxN + random.nextDouble()*2*maxN;
    }
}
