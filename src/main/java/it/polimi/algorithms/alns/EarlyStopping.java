package it.polimi.algorithms.alns;

import it.polimi.domain.Solution;

public class EarlyStopping implements StoppingCondition {
    private final int maxSegmentsWithoutImprovements;
    private int segmentsWithoutImprovement;
    private Solution best;

    public EarlyStopping(int maxSegmentsWithoutImprovements) {
        this.maxSegmentsWithoutImprovements = maxSegmentsWithoutImprovements;
        this.segmentsWithoutImprovement = 0;
        this.best = null;
    }

    @Override
    public boolean isStopping(Solution s) {
        if (best == null || s.getCost() < best.getCost()) {
            segmentsWithoutImprovement = 0;
            best = s;
        } else {
            segmentsWithoutImprovement++;
        }
        return segmentsWithoutImprovement > maxSegmentsWithoutImprovements;
    }
}
