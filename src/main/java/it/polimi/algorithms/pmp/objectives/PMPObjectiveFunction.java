package it.polimi.algorithms.pmp.objectives;

import it.polimi.domain.Location;
import it.polimi.algorithms.pmp.domain.PMPSolution;

import java.util.List;

public abstract class PMPObjectiveFunction {
    public double unassignedMedianPenalty;

    public PMPObjectiveFunction(double unassignedMedianPenalty) {
        this.unassignedMedianPenalty = unassignedMedianPenalty;
    }

    public double getUnassignedMedianPenalty() {
        return unassignedMedianPenalty;
    }

    public abstract double getCost(PMPSolution solution);
    public abstract double getInsertionDelta(Location toInsert, PMPSolution solution);
    public abstract double getRemovalDelta(List<Location> toRemove, PMPSolution solution);
}
