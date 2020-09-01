package it.polimi.algorithms.vrp.recreate;

import it.polimi.domain.routing.Job;
import it.polimi.domain.routing.VehicleRoutingProblem;

import java.util.Random;
import java.util.concurrent.ExecutorService;

public class GreedyInsertionWithNoise extends GreedyInsertion {
    private double maxN;
    private Random random;

    public GreedyInsertionWithNoise(VehicleRoutingProblem vrp, ExecutorService executorService, double maxN) {
        super(vrp, executorService);
        this.maxN = maxN;
        this.random = new Random();
    }

    public GreedyInsertionWithNoise(VehicleRoutingProblem vrp, ExecutorService executorService, double maxN, int seed) {
        super(vrp, executorService);
        this.maxN = maxN;
        this.random = new Random(seed);
    }

    @Override
    protected double getInsertionDelta(Job prev, Job next, Job cur) {
        return Math.max(0, super.getInsertionDelta(prev, next, cur) + getNoise());
    }

    private double getNoise() {
        return -maxN + random.nextDouble()*2*maxN;
    }
}
